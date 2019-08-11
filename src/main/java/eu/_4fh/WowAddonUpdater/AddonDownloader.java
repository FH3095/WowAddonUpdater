package eu._4fh.WowAddonUpdater;

import java.io.File;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import eu._4fh.WowAddonUpdater.Config.DownloadStep;
import eu._4fh.WowAddonUpdater.Util.Pair;
import eu._4fh.WowAddonUpdater.data.AddonInfo;

public class AddonDownloader {
	private final @Nonnull AddonInfo addonInfo;
	private @CheckForNull String newVersion;
	private final @Nonnull HttpClient httpClient;
	private final @Nonnull CookieManager cookieManager;

	public AddonDownloader(final @Nonnull AddonInfo addonInfo) {
		this.addonInfo = addonInfo;
		this.newVersion = null;
		this.cookieManager = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		this.httpClient = HttpClient.newBuilder().followRedirects(Redirect.NEVER).cookieHandler(cookieManager)
				.connectTimeout(Duration.ofSeconds(90)).version(Version.HTTP_1_1).build();
	}

	private @Nonnull URL executeSteps()
			throws IOException, InterruptedException, URISyntaxException, InvalidUserInputError {
		URL nextUrl = addonInfo.getFilesUrl();
		for (final DownloadStep step : Config.getInstance().getCurseSteps()) {
			nextUrl = executeStep(nextUrl, step);
		}
		return nextUrl;
	}

	private URL executeStep(final @Nonnull URL url, final @Nonnull DownloadStep step)
			throws IOException, InterruptedException, URISyntaxException, InvalidUserInputError {
		final Document doc = getHtmlPage(url);
		final Elements elements = doc.select(step.getElementsSelector());
		for (final Element element : elements) {
			boolean testsSuccessfull = true;
			for (String test : step.getTests()) {
				test = test.replace("$ReleaseType$", addonInfo.getReleaseTypeLetter());
				if (testsSuccessfull && element.selectFirst(test) == null) {
					testsSuccessfull = false;
					break;
				}
			}
			if (testsSuccessfull) {
				if (step.getVersionSelector() != null) {
					if (newVersion != null) {
						throw new InvalidUserInputError("Found new version twice. Found again for "
								+ addonInfo.getName() + " on " + url.toString());
					}
					newVersion = element.selectFirst(step.getVersionSelector()).text().trim();
				}
				final Element linkTag = element.selectFirst(step.getLinkSelector());
				return new URL(linkTag.absUrl("href"));
			}
		}
		throw new InvalidUserInputError(
				"Cant find element that matches tests for " + addonInfo.getName() + " on " + url.toString());
	}

	private @Nonnull Document getHtmlPage(final @Nonnull URL url)
			throws IOException, InterruptedException, URISyntaxException {
		final String pageHtml;
		final HttpResponse<String> resp = Util.sendHttpRequestAndFollowRedirects(httpClient, url,
				BodyHandlers.ofString());
		pageHtml = resp.body();
		final Document doc = Jsoup.parse(pageHtml, url.toString());
		return doc;
	}

	public @CheckForNull Pair<String, File> getZipFileForNewVersion()
			throws InvalidUserInputError, IOException, InterruptedException, URISyntaxException {
		Output.normal("Check " + addonInfo.getName());
		final URL downloadUrl = executeSteps();
		if (newVersion == null) {
			throw new RuntimeException("Cant find addon version for " + addonInfo.getName());
		}
		if (newVersion.equals(addonInfo.getVersion())) {
			Output.normal("Up-To-Date: " + addonInfo.getName());
			return null;
		}
		final @Nonnull File zipFile = downloadZipFile(downloadUrl);
		return new Pair<>(newVersion, zipFile);
	}

	private final @Nonnull File downloadZipFile(final @Nonnull URL url)
			throws IOException, InterruptedException, URISyntaxException {
		final @Nonnull File zipFile = Util.createTempFile();
		final HttpResponse<Path> resp = Util.sendHttpRequestAndFollowRedirects(httpClient, url,
				BodyHandlers.ofFile(zipFile.toPath()));
		resp.body();
		return zipFile;
	}
}
