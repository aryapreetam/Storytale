function initGalleryFallbacks() {
  var wrappers = document.querySelectorAll(".wasm-gallery-wrapper");
  wrappers.forEach(function (wrapper) {
    var container = wrapper.querySelector(".storytale-gallery-container");
    var iframe = wrapper.querySelector(".storytale-gallery-iframe");
    var fallback = wrapper.querySelector(".storytale-gallery-fallback");
    var link = wrapper.querySelector(".storytale-gallery-link");

    if (!container || !iframe || !fallback) return;

    var preferredUrl =
      (iframe && (iframe.getAttribute("data-src") || iframe.getAttribute("src"))) ||
      wrapper.getAttribute("data-gallery-url");

    if (!preferredUrl) return;

    function checkLocalEndpoint(url) {
      return new Promise(function (resolve) {
        try {
          var u = new URL(url, window.location.href);
          var wsUrl = (u.protocol === "https:" ? "wss://" : "ws://") + u.host + "/ws";
          var socket = new WebSocket(wsUrl);
          var timer = setTimeout(function () {
            try { socket.close(); } catch (e) {}
            resolve(false);
          }, 800);

          socket.onopen = function () {
            clearTimeout(timer);
            try { socket.close(); } catch (e) {}
            resolve(true);
          };
          socket.onerror = function () {
            clearTimeout(timer);
            resolve(false);
          };
        } catch (e) {
          resolve(false);
        }
      });
    }

    function checkRemoteEndpoint(url) {
      return fetch(url, { method: "HEAD" })
        .then(function (res) { return res.ok; })
        .catch(function () { return false; });
    }

    function checkEndpoint(targetUrl) {
      var isLocal = targetUrl.indexOf("localhost") !== -1 || targetUrl.indexOf("127.0.0.1") !== -1;
      if (isLocal) {
        return checkLocalEndpoint(targetUrl);
      }
      return checkRemoteEndpoint(targetUrl);
    }

    function resolveActiveUrl(targetUrl) {
      return checkEndpoint(targetUrl).then(function (isAlive) {
        if (isAlive) return targetUrl;

        var isDocLocal =
          window.location.hostname === "localhost" ||
          window.location.hostname === "127.0.0.1";

        if (isDocLocal && targetUrl.indexOf("localhost") === -1 && targetUrl.indexOf("127.0.0.1") === -1) {
          var localCandidate = "http://localhost:8080/";
          return checkLocalEndpoint(localCandidate).then(function (isLocalAlive) {
            if (isLocalAlive) return localCandidate;
            return null;
          });
        }
        return null;
      });
    }

    resolveActiveUrl(preferredUrl).then(function (activeUrl) {
      if (activeUrl) {
        container.style.display = "block";
        fallback.style.display = "none";

        if (iframe.getAttribute("loading") === "lazy") {
          iframe.removeAttribute("loading");
        }

        if (!iframe.src || iframe.src === "about:blank" || iframe.src !== activeUrl) {
          iframe.src = activeUrl;
        }

        if (link) {
          link.href = activeUrl;
        }
      } else {
        container.style.display = "none";
        fallback.style.display = "block";
      }
    });
  });
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initGalleryFallbacks);
} else {
  initGalleryFallbacks();
}

if (typeof document$ !== "undefined") {
  document$.subscribe(function () {
    initGalleryFallbacks();
  });
}

document.addEventListener("change", function (e) {
  if (e.target && e.target.matches && e.target.matches(".tabbed-set input")) {
    initGalleryFallbacks();
  }
});
