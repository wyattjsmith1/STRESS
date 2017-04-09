package edu.calpoly.apacheprojectdata.web;

import com.github.mkopylec.recaptcha.validation.ErrorCode;
import com.github.mkopylec.recaptcha.validation.RecaptchaValidator;
import com.github.mkopylec.recaptcha.validation.ValidationResult;
import edu.calpoly.apacheprojectdata.metrics.MetricsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles search requests.
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private RecaptchaValidator recaptchaValidator;

    @RequestMapping(value = "/{snapshot}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List search(HttpServletRequest request, @RequestBody FilterSearch filters, @PathVariable("snapshot") Integer snapshot) {
        ValidationResult result = recaptchaValidator.validate(filters.getCaptcha());
        if (result.isSuccess()) {
            return MetricsManager.getInstance().search(filters.getFilters(), snapshot);
        }
        return Collections.singletonList(result.getErrorCodes().stream().map(ErrorCode::getText).collect(Collectors.toList()));
    }
}
