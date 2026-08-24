package io.telekom.orchest.orchestrest.utils;

import io.telekom.orchest.orchestrest.api.dto.ResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Utility class for building REST API response DTOs. Provides helper methods to create standardized
 * response objects with consistent structure.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RestUtils {

  /**
   * Builds a response DTO with data, HTTP status code, and message.
   *
   * @param <T> The type of data in the response.
   * @param data The response data payload.
   * @param code The HTTP status code.
   * @param msg The response message.
   * @return A ResponseDTO instance containing the data and metadata.
   */
  public static <T> ResponseDTO<T> buildResponse(T data, int code, String msg) {
    ResponseDTO<T> responseDTO = new ResponseDTO<>();
    responseDTO.setData(data);
    responseDTO.setMeta(meta(code, msg));
    return responseDTO;
  }

  /**
   * Builds a successful response DTO with HTTP 200 status code.
   *
   * @param <T> The type of data in the response.
   * @param data The response data payload.
   * @return A ResponseDTO instance with success status (200) and the provided data.
   */
  public static <T> ResponseDTO<T> success(T data) {
    ResponseDTO<T> responseDTO = new ResponseDTO<>();
    responseDTO.setData(data);
    responseDTO.setMeta(buildSuccessMeta());
    return responseDTO;
  }

  /**
   * Creates a success metadata object with HTTP 200 status code.
   *
   * @return A Meta object with code 200 and message "success".
   */
  private static ResponseDTO.Meta buildSuccessMeta() {
    return new ResponseDTO.Meta(200, "success");
  }

  /**
   * Builds a response DTO with only metadata (no data payload). Useful for error responses or
   * status-only responses.
   *
   * @param <T> The type parameter for the response DTO.
   * @param code The HTTP status code.
   * @param msg The response message.
   * @return A ResponseDTO instance with only metadata, no data.
   */
  public static <T> ResponseDTO<T> buildMeta(int code, String msg) {
    ResponseDTO<T> responseDTO = new ResponseDTO<>();
    responseDTO.setMeta(meta(code, msg));
    return responseDTO;
  }

  /**
   * Creates a metadata object with the specified code and message.
   *
   * @param code The HTTP status code.
   * @param message The response message.
   * @return A Meta object with the specified code and message.
   */
  private static ResponseDTO.Meta meta(int code, String message) {
    return new ResponseDTO.Meta(code, message);
  }
}
