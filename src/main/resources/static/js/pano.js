var $hotSpotsList = $("#settings-hotspot-list");
var $tourList = $("#tour-list-container");
var $sceneList = $("#scene-list-container");
var $centerPoint = $("#center-point");

var tours = [];
var sceneViewerList = {};

var scenes = {};

var defaultPreviewSettings = {
  "autoLoad": true,
  "showZoomCtrl": false,
  "keyboardZoom": false,
  "mouseZoom": false,
  "showFullscreenCtrl": false,
  "showControls": false,
  "type": "multires"
};

var viewer;
var hsViewer;

getTours();

function initMainViewer(tour) {
  if (viewer) {
      destroyPano(viewer);
  }

  scenes = {};

  tour.scenes.forEach(function(scene) {
    scenes[scene.id] = scene;
  });

  var firstScene = tour.scenes[0].id;

  viewer = pannellum.viewer('panorama', {
    "autoLoad": true,
    "compass": true,

    "default": {
      "firstScene": firstScene,
      "author": "Demo",
      "sceneFadeDuration": 1000
    },

    "scenes": scenes
  });

  viewer.on("load", loadSceneConfig);

  loadSceneConfig();
}

function hotspot(hotSpotDiv, args) {
  var argsArr = args.split("|");
  var sceneId = argsArr[0];
  var content = argsArr[1];

  var div = document.createElement('div');
  div.classList.add('custom-hotspot-wrapper');
  div.classList.add('custom-tooltip');
  div.classList.add('custom-hotspot-target');

  var span = document.createElement('span');
  span.innerHTML = content;
  hotSpotDiv.appendChild(span);
  hotSpotDiv.appendChild(div);
  span.style.width = span.scrollWidth - 20 + 'px';
  span.style.marginLeft = -(span.scrollWidth - div.offsetWidth) / 2 + 'px';
  span.style.marginTop = -span.scrollHeight - 12 + 'px';
}

function loadSceneConfig() {

  if (hsViewer) {
    destroyPano(hsViewer);
  }

  $hotSpotsList.empty();
  populateSceneList();

  var hotSpots = getHotSpotsFromView(viewer);

  hotSpots.forEach(function(hotspot) {
    $hotSpotsList
      .append($("<li>").attr('id', "li-" + hotspot.sceneId)
        .click(function() {switchHotSpot(hotspot)})
        .append($("<div>").addClass('collapsible-header').text(hotspot.sceneId))
        .append($("<div>").addClass('collapsible-body')
          .append(loadHotSpotSettings(hotspot))));
  });
}

/**
 return a element
 */
function loadHotSpotSettings(hotspot) {
  return $("<form>")
    .append($("<div>").addClass('row center-align')
      .append($("<a>").addClass("waves-effect waves-light btn")
        .text("Set Pitch & Yaw to Center")
        .append($("<i>").addClass("material-icons left").text("adjust"))
        .click(function(){useCurrentCenter(hotspot)})))
    .append($("<div>").addClass('row')
      .append($("<div>").addClass('input-field col s12')
        .append($("<input>")
          .addClass('validate')
          .attr("id", "hotspot-text-" + hotspot.sceneId)
          .attr("name", "hotspot-text-" + hotspot.sceneId)
          .attr("type", "text")
          .val(hotspot.text))
        .append($("<label>")
          .attr("for", "hotspot-text-" + hotspot.sceneId)
          .text("Text"))))
    .append($("<div>").addClass('row')
      .append($("<p>").addClass('range-field col s12')
        .append($("<label>")
          .attr("for", "hotspot-pitch-" + hotspot.sceneId)
          .text("HotSpot Pitch in Current Scene"))
        .append($("<input>")
          .attr("id", "hotspot-pitch-" + hotspot.sceneId)
          .attr("name", "hotspot-pitch-" + hotspot.sceneId)
          .attr("type", "range")
          .attr("min", -360)
          .attr("max", 360)
          .val(hotspot.pitch)
          .change(function(e) { updateHotSpotValue({pitch: +e.target.value}, hotspot); }))))
    .append($("<div>").addClass('row')
      .append($("<p>").addClass('range-field col s12')
        .append($("<label>")
          .attr("for", "hotspot-yaw-" + hotspot.sceneId)
          .text("HotSpot Yaw in Current Scene"))
        .append($("<input>")
          .attr("id", "hotspot-yaw-" + hotspot.sceneId)
          .attr("name", "hotspot-yaw-" + hotspot.sceneId)
          .attr("type", "range")
          .attr("min", -360)
          .attr("max", 360)
          .val(hotspot.yaw)
          .change(function(e) { updateHotSpotValue({yaw: +e.target.value}, hotspot); }))))
    .append($("<div>").addClass('row')
      .append($("<p>").addClass('range-field col s12')
        .append($("<label>")
          .attr("for", "hotspot-targetPitch-" + hotspot.sceneId)
          .text("HotSpot Pitch in Connected Scene"))
        .append($("<input>")
          .attr("id", "hotspot-targetPitch-" + hotspot.sceneId)
          .attr("name", "hotspot-targetPitch-" + hotspot.sceneId)
          .attr("type", "range")
          .attr("min", -360)
          .attr("max", 360)
          .val(hotspot.targetPitch)
          .change(function(e) { updateHotSpotValue({targetPitch: +e.target.value}, hotspot); }))))
    .append($("<div>").addClass('row')
      .append($("<p>").addClass('range-field col s12')
        .append($("<label>")
          .attr("for", "hotspot-targetYaw-" + hotspot.sceneId)
          .text("HotSpot Yaw in Connected Scene"))
        .append($("<input>")
          .attr("id", "hotspot-targetYaw-" + hotspot.sceneId)
          .attr("name", "hotspot-targetYaw-" + hotspot.sceneId)
          .attr("type", "range")
          .attr("min", -360)
          .attr("max", 360)
          .val(hotspot.targetYaw)
          .change(function(e) { updateHotSpotValue({targetYaw: +e.target.value}, hotspot); }))))
    .append($("<div>").addClass('row center-align')
      .append($("<a>").addClass("waves-effect waves-light btn red")
        .text("Delete HotSpot")
        .append($("<i>").addClass("material-icons left").text("delete"))
        .click(function(){removeHotSpot(hotspot);})))
}

function updateHotSpotValue(val, hotspot) {
  viewer.removeHotSpot(hotspot.id);

  var hs = Object.assign(hotspot, val);

  viewer.addHotSpot(hs);

  if (val.hasOwnProperty("targetPitch") || val.hasOwnProperty("targetYaw")) {
    updateHotSpotViewer(hs);
  }
}

function updateHotSpotViewer(hotspot) {

  hsViewer.setPitch(hotspot.targetPitch);
  hsViewer.setYaw(hotspot.targetYaw);
}

function initHotSpotViewer(hotspot) {
  if (hsViewer) {
    hsViewer.destroy();
    hsViewer = undefined;
  }

  var scene = findSceneById(hotspot.sceneId);

  var hsViewerOptions = Object.assign({
    "multiRes": scene.multiRes,
    "yaw": hotspot.targetYaw,
    "pitch": hotspot.targetPitch,
    "id": hotspot.sceneId
  }, defaultPreviewSettings);

  hsViewer = pannellum.viewer('hotspot-pano-container', hsViewerOptions);

  hsViewer.on("mouseup", function () {
    updateHotSpotValue({
      targetPitch: hsViewer.getPitch(),
      targetYaw: hsViewer.getYaw()
    }, hotspot);

    $("#hotspot-targetPitch-" + hotspot.sceneId).val(hsViewer.getPitch());
    $("#hotspot-targetYaw-" + hotspot.sceneId).val(hsViewer.getYaw());
  });
}

function findSceneById(sceneId) {
  var index = Object.keys(scenes).filter(function(key) {
    return key === sceneId;
  })[0];
  return scenes[index];
}

function getHotSpotsFromView(viewer) {
  return viewer.getConfig().hotSpots;
}

function switchHotSpot(hotspot) {
  viewer.setPitch(hotspot.pitch);
  viewer.setYaw(hotspot.yaw);

  if (!hsViewer || hsViewer.getConfig().id !== hotspot.sceneId) {
    initHotSpotViewer(hotspot);
  }
}

function useCurrentCenter(hotspot) {
  updateHotSpotValue({
    pitch: viewer.getPitch(),
    yaw: viewer.getYaw()
  }, hotspot);
}

function populateSceneList() {
  $sceneList.empty();

  var hotSpots = getHotSpotsFromView(viewer);
  var connectedSceneIds = hotSpots.map(function(hs) {
    return hs.sceneId;
  });

  Object.keys(scenes)
    .filter(function(sceneId) {
      return sceneId !== viewer.getScene() && connectedSceneIds.indexOf(sceneId) === -1;
    })
    .forEach(function(sceneId) {
      var id = "scene-preview-" + sceneId;
      var scene = scenes[sceneId];

      $sceneList.append($("<div>").addClass('card')
        .append($("<div>").addClass('card-image')
          .append($("<div>").attr('id', id).addClass("scene-preview"))
          .append($("<span>").addClass('card-title').text(scene.title)))
        .append($("<div>").addClass('card-action')
          .append($("<a>").append($("<i>").addClass("material-icons").text("add_circle_outline"))
            .click(function() { addToScene(scene);}))
        ));

      var viewerOptions = Object.assign({
        "multiRes": scene.multiRes,
        "id": sceneId
      }, defaultPreviewSettings);

      sceneViewerList[sceneId] = pannellum.viewer(id, viewerOptions);
    });
}

function addToScene(scene) {
  var currentSceneId = viewer.getScene();
  var hs = {
    "id": currentSceneId + "-" + scene.id,
    "pitch": viewer.getPitch(),
    "yaw": viewer.getYaw(),
    "type": "scene",
    "text": scene.id,
    "sceneId": scene.id,
    "targetYaw": scene.yaw,
    "targetPitch": scene.pitch,
    "cssClass": "custom-hotspot",
    "createTooltipFunc": hotspot,
    "createTooltipArgs": scene.id + "|" + scene.id
  };

  viewer.addHotSpot(hs, currentSceneId);

  syncNewHotSpotWithSceneList(hs, currentSceneId);
  populateSceneList();
}

function syncNewHotSpotWithSceneList(newHs, sceneId) {
  loadSceneConfig();

  var currentHotSpotsInScene = scenes[sceneId].hotSpots;
  var hotSpotExists = currentHotSpotsInScene.filter(function(hs) {
    return hs.sceneId === newHs.sceneId;
  }).length > 0;

  if (hotSpotExists) return;

  currentHotSpotsInScene.push(newHs);
}

function syncRemovedHotSpotWithSceneList(removedHs, sceneId) {
  loadSceneConfig();

  var currentHotSpotsInScene = scenes[sceneId].hotSpots;
  scenes[sceneId].hotSpots = currentHotSpotsInScene.filter(function (hs) {
    return hs.id !== removedHs.id;
  });
}

function removeHotSpot(hs) {
  viewer.removeHotSpot(hs.id);
  syncRemovedHotSpotWithSceneList(hs, viewer.getScene());
}

function getTours() {
  $.getJSON(apiUrl + "/public/guest/tours")
    .done(function (data) {
      tours = data;
      tours.forEach(function (tour) {
        $tourList.append($("<li>").addClass("collection-item").attr('id', "tour-list-item-" +tour.name)
          .append($("<div>").text(tour.name)
            .append($("<a>").attr("href", "#switch-tour").addClass("secondary-content")
              .click(function() {switchTour(tour.name);})
              .append($("<i>").addClass("material-icons").text("arrow_forward")))
            .append($("<a>").attr("href", "#save-tour").addClass("secondary-content hide action-save").attr('id', "tour-list-item-action-save-" + tour.name)
              .click(function() {saveTour(tour.name);})
              .append($("<i>").addClass("material-icons").text("save")))
          ));
      });
    });
}

function switchTour(name) {
  if ($centerPoint.hasClass("hide")) {
    $centerPoint.removeClass("hide");
  }
  $tourList.find('.collection-item').removeClass('active');
  $tourList.find('#tour-list-item-' + name).addClass('active');
  $tourList.find('.action-save').addClass('hide');
  $tourList.find('#tour-list-item-action-save-' + name).removeClass('hide');

  var tour = findTourByName(name);

  addMetaToHotSpots(tour);

  initMainViewer(tour);
}

function saveTour(name) {
  var tour = findTourByName(name);
  tour.scenes = Object.keys(scenes)
    .map(function (sceneId) {
      return scenes[sceneId];
    });
  tour = removeMetaFromHotSpots(tour);

  $.ajax(apiUrl + "/public/guest/tours/" + name, {
    method: "PUT",
    data: JSON.stringify(tour),
    dataType: "json",
    contentType: "application/json"
  })
    .done(function (res) {
      console.log(res);
    })

}

function findTourByName(name) {
  return tours.filter(function (tour) {
    return tour.name === name;
  })[0];
}

function removeMetaFromHotSpots(tour) {
  var cleanedTour = JSON.parse(JSON.stringify(tour));
  cleanedTour.scenes.forEach(function(scene) {
    scene.hotSpots.forEach(function(hotspot) {
      delete hotspot.cssClass;
      delete hotspot.createTooltipArgs;
      delete hotspot.div;
    });
  });
  return cleanedTour;
}

function addMetaToHotSpots(tour) {
  tour.scenes.forEach(function(scene) {
    scene.hotSpots.forEach(function(hs) {
      Object.assign(hs, {
        "cssClass": "custom-hotspot",
        "createTooltipFunc": hotspot,
        "createTooltipArgs": scene.id + "|" + scene.id
      });
    });
  });
}

function destroyPano(view) {
    view.destroy();
    view = undefined;
}