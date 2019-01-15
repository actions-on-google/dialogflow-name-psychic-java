/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import com.google.actions.api.ActionRequest;
import com.google.actions.api.ActionResponse;
import com.google.actions.api.Capability;
import com.google.actions.api.ConstantsKt;
import com.google.actions.api.DialogflowApp;
import com.google.actions.api.ForIntent;
import com.google.actions.api.response.ResponseBuilder;
import com.google.actions.api.response.helperintent.NewSurface;
import com.google.actions.api.response.helperintent.Permission;
import com.google.api.services.actions_fulfillment.v2.model.Image;
import com.google.api.services.actions_fulfillment.v2.model.LatLng;
import com.google.api.services.actions_fulfillment.v2.model.Surface;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.AddressComponentType;
import com.google.maps.model.GeocodingResult;
import java.util.Collections;
import java.util.Map;
import java.util.ResourceBundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyActionsApp extends DialogflowApp {

  private static final Logger LOGGER = LoggerFactory.getLogger(MyActionsApp.class);

  private static final String STORAGE_KEY_LOCATION = "location";
  private static final String STORAGE_KEY_NAME = "name";

  private static final String DATA_KEY_REQUESTED_PERMISSION = "requestedPermission";

  // Note: Do not store any state as an instance variable.
  // It is ok to have final variables where the variable is assigned a value in
  // the constructor but remains unchanged. This is required to ensure thread-
  // safety as the entry point (ActionServlet) instances may
  // be reused by the server.

  @ForIntent("Default Welcome Intent")
  public ActionResponse welcome(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);

    // Map<String, Object> storage = response.getUserStorage();
    // if (storage != null) {
    //   storage.clear();
    // }
    // Uncomment above to delete the cached permissions on each request
    // to force the app to request new permissions from the user

    response.add(formatResponse("greet_user"));
    return response.build();
  }

  @ForIntent("Unrecognized Deep Link Fallback")
  public ActionResponse deepLinkFallback(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);
    response.add(formatResponse("unhandled_deep_links", request.getRawText()));
    return response.build();
  }

  @ForIntent("request_name_permission")
  public ActionResponse requestNamePermission(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);

    String requestedPermission = ConstantsKt.PERMISSION_NAME;

    response.getConversationData().put(DATA_KEY_REQUESTED_PERMISSION, requestedPermission);

    String storageKey = STORAGE_KEY_NAME;

    if (!request.getUserStorage().containsKey(storageKey)) {
      Permission permission = new Permission()
          .setContext(formatResponse("permission_reason"))
          .setPermissions(new String[]{requestedPermission});
      response.add("PLACEHOLDER_FOR_PERMISSION");
      response.add(permission);
    } else {
      String name = (String) request.getUserStorage().get(storageKey);
      response.add(formatResponse("say_name", name));
      response.endConversation();
    }

    return response.build();
  }


  @ForIntent("request_location_permission")
  public ActionResponse requestLocationPermission(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);

    // If the request comes from a phone, we can't use coarse location.
    String requestedPermission = request.hasCapability(Capability.SCREEN_OUTPUT.getValue()) ?
        ConstantsKt.PERMISSION_DEVICE_PRECISE_LOCATION
        : ConstantsKt.PERMISSION_DEVICE_COARSE_LOCATION;

    response.getConversationData().put(DATA_KEY_REQUESTED_PERMISSION, requestedPermission);

    if (!request.getUserStorage().containsKey(STORAGE_KEY_LOCATION)) {
      Permission permission = new Permission()
          .setContext(formatResponse("permission_reason"))
          .setPermissions(new String[]{requestedPermission});
      response.add("PLACEHOLDER_FOR_PERMISSION");
      response.add(permission);
    } else {
      showLocationOnScreen(request, response);
    }

    return response.build();
  }


  @ForIntent("handle_permission")
  public ActionResponse handlePermission(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);

    boolean permissionGranted = request.getArgument(ConstantsKt.ARG_PERMISSION).getBoolValue();
    if (!permissionGranted) {
      LOGGER.error("Permission not granted");
      response.add(formatResponse("read_mind_error"));
      response.endConversation();
      return response.build();
    }

    Map<String, Object> storage = response.getUserStorage();

    String requestedPermission = (String) request.getConversationData()
        .get(DATA_KEY_REQUESTED_PERMISSION);
    if (requestedPermission.equals(ConstantsKt.PERMISSION_NAME)) {
      String name = request.getUser().getProfile().getDisplayName();
      storage.put(STORAGE_KEY_NAME, name);
      response.add(formatResponse("say_name", name));
      response.endConversation();
      return response.build();
    }
    if (requestedPermission.equals(ConstantsKt.PERMISSION_DEVICE_COARSE_LOCATION)) {
      // If we requested coarse location, it means that we're on a speaker device.
      String location = request.getDevice().getLocation().getCity();
      storage.put(STORAGE_KEY_LOCATION, location);
      showLocationOnScreen(request, response);
      return response.build();
    }
    if (requestedPermission.equals(ConstantsKt.PERMISSION_DEVICE_PRECISE_LOCATION)) {
      // If we requested precise location, it means that we're on a phone.
      // Because we will get only latitude and longitude, we need to
      // reverse geocode to get the city.
      LatLng latLng = request.getDevice().getLocation().getCoordinates();

      try {
        String city = coordinatesToCity(latLng.getLatitude(), latLng.getLongitude());
        String key = STORAGE_KEY_LOCATION;
        request.getUserStorage().put(key, city);
        response.getUserStorage().put(key, city);
        showLocationOnScreen(request, response);
        return response.build();
      } catch (Exception e) {
        LOGGER.error(e.toString());
        response.add(formatResponse("read_mind_error"));
        response.endConversation();
        return response.build();
      }
    }

    LOGGER.error("Unrecognized permission");
    response.add(formatResponse("read_mind_error"));
    response.endConversation();
    return response.build();
  }

  @ForIntent("new_surface")
  public ActionResponse newSurface(ActionRequest request) {
    ResponseBuilder response = getResponseBuilder(request);
    sayLocation(response, (String) request.getUserStorage().get(STORAGE_KEY_LOCATION));
    return response.build();
  }

  /**
   * Gets the city name from results returned by Google Maps reverse geocoding from coordinates.
   */
  private String coordinatesToCity(double latitude, double longitude) throws Exception {
    GeoApiContext context = new GeoApiContext.Builder()
        .apiKey(retrieveMapsKey())
        .build();

    com.google.maps.model.LatLng latLng = new com.google.maps.model.LatLng(latitude, longitude);

    GeocodingResult[] results = GeocodingApi.reverseGeocode(context, latLng).await();
    AddressComponent[] components = results[0].addressComponents;
    for (AddressComponent component : components) {
      for (AddressComponentType type : component.types) {
        if (type.toString().equals("locality")) {
          return component.longName;
        }
      }
    }
    throw new Exception("Could not parse city name from Google Maps results");
  }

  /**
   * Shows the location of the user with a preference for a screen device. If on a speaker device,
   * asks to transfer dialog to a screen device. Reads location from userStorage.
   */
  private void showLocationOnScreen(ActionRequest request, ResponseBuilder response) {
    String capability = Capability.SCREEN_OUTPUT.getValue();
    boolean availableHasCapability = false;
    for (Surface surface : request.getAvailableSurfaces()) {
      if (surface.getCapabilities().contains(capability)) {
        availableHasCapability = true;
        break;
      }
    }
    if (request.hasCapability(capability) || !availableHasCapability) {
      String location = (String) request.getUserStorage().get(STORAGE_KEY_LOCATION);
      sayLocation(response, location);
      return;
    }

    NewSurface newSurface = new NewSurface()
        .setContext(formatResponse("new_surface_context"))
        .setNotificationTitle(formatResponse("notification_text"))
        .setCapabilities(Collections.singletonList(capability));
    response.add("PLACEHOLDER_FOR_NEW_SURFACE");
    response.add(newSurface);
  }

  private void sayLocation(ResponseBuilder response, String city) {
    String key = retrieveMapsKey();
    String url =
        "https://maps.googleapis.com/maps/api/staticmap?size=640x640&key=" + key + "&center="
            + city;
    response.add(formatResponse("say_location", city));
    Image image = new Image()
        .setUrl(url)
        .setAccessibilityText("City Map");
    response.add(image);
    response.endConversation();
  }

  private String retrieveMapsKey() {
    ResourceBundle bundle = ResourceBundle.getBundle("config");
    return bundle.getString("maps_key");
  }

  private String readResponse(String response) {
    ResourceBundle bundle = ResourceBundle.getBundle("resources");
    return bundle.getString(response);
  }

  private String formatResponse(String response) {
    return readResponse(response);
  }

  private String formatResponse(String response, String argument) {
    return String.format(readResponse(response), argument);
  }
}
