package com.engineerpro.instagram.exception.handler;

import com.engineerpro.instagram.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice()
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {
	private static final String ERROR_CODE_INTERNAL = "INTERNAL_ERROR";
	private static final Map<Class<? extends RuntimeException>, HttpStatus> EXCEPTION_TO_HTTP_STATUS_CODE = Map.of(
			UserNotFoundException.class, HttpStatus.NOT_FOUND,
      AuthenticationException.class, HttpStatus.UNAUTHORIZED,
			PostNotFoundException.class, HttpStatus.NOT_FOUND,
			CommentNotFoundException.class, HttpStatus.NOT_FOUND,
			NoPermissionException.class, HttpStatus.FORBIDDEN,
      UsernameRegisteredException.class, HttpStatus.BAD_REQUEST
  );

	private static final Map<Class<? extends RuntimeException>, String> EXCEPTION_TO_ERROR_CODE = Map.of(
			UserNotFoundException.class, "USER_NOT_FOUND",
      AuthenticationException.class, "UNAUTHORIZED_REQUEST",
			PostNotFoundException.class, "POST_NOT_FOUND",
			CommentNotFoundException.class, "COMMENT_NOT_FOUND",
			NoPermissionException.class, "NO_PERMISSION",
      UsernameRegisteredException.class, "This user name is registered, please use another one"
  );

	@ExceptionHandler()
	ResponseEntity<ApiExceptionResponse> handleUserNotFoundException(RuntimeException exception) {
		HttpStatus httpStatus = EXCEPTION_TO_HTTP_STATUS_CODE.getOrDefault(exception.getClass(),
				HttpStatus.INTERNAL_SERVER_ERROR);
		String errorCode = EXCEPTION_TO_ERROR_CODE.getOrDefault(exception.getClass(), ERROR_CODE_INTERNAL);

		final ApiExceptionResponse response = ApiExceptionResponse.builder().status(httpStatus).errorCode(errorCode)
				.build();

		return ResponseEntity.status(response.getStatus()).body(response);
	}

}
