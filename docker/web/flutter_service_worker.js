'use strict';
const MANIFEST = 'flutter-app-manifest';
const TEMP = 'flutter-temp-cache';
const CACHE_NAME = 'flutter-app-cache';
const RESOURCES = {
  "assets/AssetManifest.json": "81d86587a0304f500a7bc2054b1c4674",
"assets/assets/background.jpg": "900a6bb82a1786f7162b638b4d3112a1",
"assets/assets/blank_logo.png": "18423cd56fae957be1810cc67c5fdb5c",
"assets/assets/cube.png": "e53a287531f83d2fa776d6bec81e1aa4",
"assets/assets/docker.jpg": "74a8b29a75ffc899d673a5ece7ea0c7e",
"assets/assets/docker_logo.jpg": "6b2c88064ca7f4f875d4eb0247f1470d",
"assets/assets/emulator.png": "39c84b041cd135eac357e0b36d7e0f84",
"assets/assets/favicon.png": "db0543debeda86980954878c78cf511a",
"assets/assets/flutter.png": "d21f1eecaeaab081ba7efec1721c0712",
"assets/assets/flutter_cube.png": "44cd9348f5e805569220205b8f3c4017",
"assets/assets/flutter_video.png": "fa0747714ecbb66c7642d88ac7b4337d",
"assets/assets/java_logo.jpg": "963e47f6f5d0a34c840a1de72e5f2d2e",
"assets/assets/logo.PNG": "7bea28eb64e6655c9c4811ca7a80b97f",
"assets/assets/me.jpg": "d286b3d019d02c41ccbb2b4e64ce9541",
"assets/assets/postgres.png": "4f4f3447e55fe7f2b698cdc4484caf74",
"assets/assets/quarks.png": "c392ae75eac49714ec2a13394abedb57",
"assets/assets/quarkus.png": "142e7c30bc4a4b0fa5511403d34004e7",
"assets/assets/quarkus_logo.png": "35e5be178a2d513f446a837d6990be4b",
"assets/assets/q_path.png": "76a90344d7061d0db84c03dc53e2529e",
"assets/assets/rss_memory.png": "4f217cc247d59a39472fe3f5d7174a2c",
"assets/assets/spring.png": "c4eb0839f9b116e6512b35c548669ffd",
"assets/assets/vobacom.png": "258e6ec4773d7eb535f2b0c48f71f3b2",
"assets/FontManifest.json": "4070b51be51f89ff5031ca702ac291d3",
"assets/fonts/EncodeSansSemiExpanded-Thin.ttf": "76b793b10f50ff5f9ac26c6e0c7160ba",
"assets/fonts/JuliusSansOne-Regular.ttf": "3dcf0ae6a78a6a64ae1e3f2406cefa3b",
"assets/fonts/MaterialIcons-Regular.ttf": "56d3ffdef7a25659eab6a68a3fbfaf16",
"assets/fonts/Montserrat-Regular.ttf": "ee6539921d713482b8ccd4d0d23961bb",
"assets/NOTICES": "e048170e4c407d02f9b282527ba33333",
"assets/packages/cupertino_icons/assets/CupertinoIcons.ttf": "115e937bb829a890521f72d2e664b632",
"assets/packages/font_awesome_flutter/lib/fonts/fa-brands-400.ttf": "5a37ae808cf9f652198acde612b5328d",
"assets/packages/font_awesome_flutter/lib/fonts/fa-regular-400.ttf": "2bca5ec802e40d3f4b60343e346cedde",
"assets/packages/font_awesome_flutter/lib/fonts/fa-solid-900.ttf": "2aa350bd2aeab88b601a593f793734c0",
"favicon.png": "db0543debeda86980954878c78cf511a",
"icons/logo.png": "7bea28eb64e6655c9c4811ca7a80b97f",
"index.html": "14b764a05129ff741dea60ae41c0ed33",
"/": "14b764a05129ff741dea60ae41c0ed33",
"main.dart.js": "b927c928e33242f0ad4fc70b42e25da1",
"manifest.json": "33d1f0a24e386277574b8a2890478bb6"
};

// The application shell files that are downloaded before a service worker can
// start.
const CORE = [
  "/",
"main.dart.js",
"index.html",
"assets/NOTICES",
"assets/AssetManifest.json",
"assets/FontManifest.json"];

// During install, the TEMP cache is populated with the application shell files.
self.addEventListener("install", (event) => {
  return event.waitUntil(
    caches.open(TEMP).then((cache) => {
      // Provide a no-cache param to ensure the latest version is downloaded.
      return cache.addAll(CORE.map((value) => new Request(value, {'cache': 'no-cache'})));
    })
  );
});

// During activate, the cache is populated with the temp files downloaded in
// install. If this service worker is upgrading from one with a saved
// MANIFEST, then use this to retain unchanged resource files.
self.addEventListener("activate", function(event) {
  return event.waitUntil(async function() {
    try {
      var contentCache = await caches.open(CACHE_NAME);
      var tempCache = await caches.open(TEMP);
      var manifestCache = await caches.open(MANIFEST);
      var manifest = await manifestCache.match('manifest');

      // When there is no prior manifest, clear the entire cache.
      if (!manifest) {
        await caches.delete(CACHE_NAME);
        contentCache = await caches.open(CACHE_NAME);
        for (var request of await tempCache.keys()) {
          var response = await tempCache.match(request);
          await contentCache.put(request, response);
        }
        await caches.delete(TEMP);
        // Save the manifest to make future upgrades efficient.
        await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
        return;
      }

      var oldManifest = await manifest.json();
      var origin = self.location.origin;
      for (var request of await contentCache.keys()) {
        var key = request.url.substring(origin.length + 1);
        if (key == "") {
          key = "/";
        }
        // If a resource from the old manifest is not in the new cache, or if
        // the MD5 sum has changed, delete it. Otherwise the resource is left
        // in the cache and can be reused by the new service worker.
        if (!RESOURCES[key] || RESOURCES[key] != oldManifest[key]) {
          await contentCache.delete(request);
        }
      }
      // Populate the cache with the app shell TEMP files, potentially overwriting
      // cache files preserved above.
      for (var request of await tempCache.keys()) {
        var response = await tempCache.match(request);
        await contentCache.put(request, response);
      }
      await caches.delete(TEMP);
      // Save the manifest to make future upgrades efficient.
      await manifestCache.put('manifest', new Response(JSON.stringify(RESOURCES)));
      return;
    } catch (err) {
      // On an unhandled exception the state of the cache cannot be guaranteed.
      console.error('Failed to upgrade service worker: ' + err);
      await caches.delete(CACHE_NAME);
      await caches.delete(TEMP);
      await caches.delete(MANIFEST);
    }
  }());
});

// The fetch handler redirects requests for RESOURCE files to the service
// worker cache.
self.addEventListener("fetch", (event) => {
  var origin = self.location.origin;
  var key = event.request.url.substring(origin.length + 1);
  // Redirect URLs to the index.html
  if (event.request.url == origin || event.request.url.startsWith(origin + '/#')) {
    key = '/';
  }
  // If the URL is not the RESOURCE list, skip the cache.
  if (!RESOURCES[key]) {
    return event.respondWith(fetch(event.request));
  }
  event.respondWith(caches.open(CACHE_NAME)
    .then((cache) =>  {
      return cache.match(event.request).then((response) => {
        // Either respond with the cached resource, or perform a fetch and
        // lazily populate the cache. Ensure the resources are not cached
        // by the browser for longer than the service worker expects.
        var modifiedRequest = new Request(event.request, {'cache': 'no-cache'});
        return response || fetch(modifiedRequest).then((response) => {
          cache.put(event.request, response.clone());
          return response;
        });
      })
    })
  );
});

self.addEventListener('message', (event) => {
  // SkipWaiting can be used to immediately activate a waiting service worker.
  // This will also require a page refresh triggered by the main worker.
  if (event.data === 'skipWaiting') {
    return self.skipWaiting();
  }

  if (event.message === 'downloadOffline') {
    downloadOffline();
  }
});

// Download offline will check the RESOURCES for all files not in the cache
// and populate them.
async function downloadOffline() {
  var resources = [];
  var contentCache = await caches.open(CACHE_NAME);
  var currentContent = {};
  for (var request of await contentCache.keys()) {
    var key = request.url.substring(origin.length + 1);
    if (key == "") {
      key = "/";
    }
    currentContent[key] = true;
  }
  for (var resourceKey in Object.keys(RESOURCES)) {
    if (!currentContent[resourceKey]) {
      resources.push(resourceKey);
    }
  }
  return contentCache.addAll(resources);
}
