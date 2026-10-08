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

function syncTabbedSets() {
  var tabSets = document.querySelectorAll(".tabbed-set");
  tabSets.forEach(function (tabSet) {
    var inputs = tabSet.querySelectorAll(":scope > input[type='radio']");
    var content = tabSet.querySelector(":scope > .tabbed-content");
    if (!content) return;
    var blocks = content.querySelectorAll(":scope > .tabbed-block");
    if (inputs.length === 0 || blocks.length === 0) return;

    var activeIndex = -1;
    for (var i = 0; i < inputs.length; i++) {
      if (inputs[i].checked) {
        activeIndex = i;
        break;
      }
    }
    if (activeIndex === -1) activeIndex = 0;

    for (var j = 0; j < blocks.length; j++) {
      if (j === activeIndex) {
        blocks[j].style.setProperty("display", "block", "important");
      } else {
        blocks[j].style.setProperty("display", "none", "important");
      }
    }

    var labelsContainer = tabSet.querySelector(":scope > .tabbed-labels");
    if (labelsContainer) {
      var labels = labelsContainer.children;
      for (var k = 0; k < labels.length; k++) {
        if (k === activeIndex) {
          labels[k].style.setProperty("border-color", "var(--md-accent-fg-color)", "important");
          labels[k].style.setProperty("color", "var(--md-accent-fg-color)", "important");
        } else {
          labels[k].style.removeProperty("border-color");
          labels[k].style.removeProperty("color");
        }
      }
    }
  });
}

function runTabAndFallbackInit() {
  syncTabbedSets();
  initGalleryFallbacks();
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", runTabAndFallbackInit);
} else {
  runTabAndFallbackInit();
}

if (typeof document$ !== "undefined") {
  document$.subscribe(function () {
    runTabAndFallbackInit();
  });
}

document.addEventListener("change", function (e) {
  if (e.target && e.target.matches && e.target.matches(".tabbed-set input")) {
    syncTabbedSets();
    initGalleryFallbacks();
  }
});

document.addEventListener("click", function (e) {
  if (e.target && e.target.closest && e.target.closest(".tabbed-labels label")) {
    setTimeout(syncTabbedSets, 0);
  }
});

