package edu.calpoly.apacheprojectdata.web;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Runs Selective search
 */

@RestController
@RequestMapping("/sample")
public class SelectiveSample {

    @RequestMapping(value = "/{snapshot}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List select(@RequestBody SampleContainer sample) throws URISyntaxException, IOException, InterruptedException {
        List<String> projects = new ArrayList<>();
        Collection<String> fields = new HashSet<>();
        for (Integer i : sample.getProjects()) {
            projects.add(String.format("id = %s", i));
        }

        /* The project is done in Hibernate, but R uses MYSQL. The fields are different. */
        for (String field : sample.getFields()) {
            String[] segments = field.split("\\.");
            fields.add(segments[segments.length - 1]);
        }

        String query = "";
        if (!projects.isEmpty()) {
            query = String.format("WHERE %s", String.join(" OR ", projects));
        }

        String fieldString = String.join(" + ", fields);
        Process p = new ProcessBuilder(new String[] {"R",  "--vanilla"}).start();
        String rCode = IOUtils.toString(ClassLoader.getSystemResource("select.R"), "UTF-8")
                .replace("{{QUERY}}", query)
                .replace("{{FIELDS}}", fieldString)
                .replace("{{SAMPLE}}", projects.get(new Random().nextInt(projects.size())))
                .replace("{{COUNT}}", sample.getNumberSamples().toString());
        System.out.println(rCode);
        p.getOutputStream().write(rCode.getBytes());
        p.getOutputStream().close();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        Pattern pattern = Pattern.compile("\\[1\\]\\s*((\\d\\s*)+)");
        while ((line = reader.readLine())!= null) {
            Matcher m = pattern.matcher(line);
            if (m.matches()) {
                return Arrays.stream(m.group(1).split(" "))
                        .mapToInt(Integer::parseInt)
                        .boxed()
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
