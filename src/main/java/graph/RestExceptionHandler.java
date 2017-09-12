/**
 * Copyright (c) Nine Points Solutions, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package graph;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Throwables;

import lombok.extern.slf4j.Slf4j;


/**
 * Exception handler defining what is logged and the specific status to be returned in a
 * REST response, if an exception is thrown while processing a request
 * 
 */
@Slf4j
@Controller
public abstract class RestExceptionHandler {  //NOSONAR - Class definition and not interface
    
    /**
     * Exception handler for processing-related exceptions
     * 
     * @param exception The details of the error that was thrown
     * @return error message (String)
     * 
     */
    @ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler({ Exception.class })
    public String handleGeneralException(final Exception e) {
        logException("Exception: ", e);
        return e.toString();
    }
    
    /**
     * Exception handler for illegal arguments in the REST requests
     * 
     * @param exception The details of the error that was thrown
     * @return error message (String)
     * 
     */
    @ResponseStatus(value=HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ IllegalArgumentException.class })
    public String handleBadRequest(final String error) {
        logInfo(error);
        return error;
    }
    
    /**
     * Log the exception details to appropriate log
     * 
     * @param info The prefix to add to the exception message
     * @param exception The details of the error that was thrown
     * 
     */
    private void logException(final String info, final Exception e) {
        log.error(info + Throwables.getStackTraceAsString(e));
    }
    
    /**
     * Log the error details to appropriate log
     * 
     * @param info The error details
     * 
     */
    private void logInfo(final String info) {
        log.error("Bad request: " + info);
    }
}
