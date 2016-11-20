package org.ak.gitanalyzer.http.processor;

import org.ak.gitanalyzer.http.ServiceFacade;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Created by Andrew on 13.10.2016.
 */
public class TemplateProcessor {

    private Map<String, String> parameters;

    public TemplateProcessor(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public String processTemplate(String path) {
        return processTemplate(path, true);
    }

    private String processTemplate(String path, boolean recursive) {
        ServiceFacade facade = ServiceFacade.INSTANCE;
        try {
            ContentType contentType = facade.getType(path);
            if (contentType != ContentType.HTML && contentType != ContentType.TEXT) {
                throw new IllegalArgumentException("Invalid file: " + path + ". Only text files supported for templates.");
            }
            String content = facade.getStringContent(path);
            if (content == null) {
                return facade.get404Content(path);
            }

            return recursive ? parse(content) : content;

        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace(System.out);
            return facade.get500Content(e);
        }
    }

    private String parse(String content) {
        StringBuilder sb = new StringBuilder();
        int startIndex = 0, endIndex;
        boolean insidePlaceholder = false;
        for (int i = 0; i < content.length(); ++i) {
            char c = content.charAt(i);
            switch (c) {
                case '{':
                    if (insidePlaceholder) {
                        sb.append(content.substring(startIndex, i));
                    }
                    startIndex = i;
                    insidePlaceholder = true;
                    break;
                case '}':
                    if (insidePlaceholder) {
                        endIndex = i;
                        String placeholder = content.substring(startIndex, endIndex + 1);
                        String substitution = getSubstitution(placeholder);
                        if (substitution != null) {
                            sb.append(substitution);
                        } else {
                            sb.append(placeholder);
                        }
                    } else {
                        sb.append(c);
                    }
                    insidePlaceholder = false;
                    break;
                default:
                    if (!insidePlaceholder) {
                        sb.append(c);
                    }
            }
        }
        if (insidePlaceholder) {
            sb.append(content.substring(startIndex));
        }
        return sb.toString();
    }

    private String getSubstitution(String placeholder) {
        placeholder = placeholder.substring(1, placeholder.length() - 1).trim();
        //functions
        for (Functions f : Functions.values()) {
            if (placeholder.startsWith(f.name())) {
                String parameter = extractParameter(placeholder);
                return f.transformation.apply(this, parameter);
            }
        }
        //find parameters
        boolean recursive = false;
        int recursiveArgIndex = placeholder.indexOf(" -r");
        if (recursiveArgIndex != -1) {
            placeholder = placeholder.substring(0, recursiveArgIndex).trim();
            recursive = true;
        }
        //nested templates
        if (placeholder.endsWith(".html")) {
            return processTemplate(placeholder, recursive);
        }
        return null;
    }

    private String extractParameter(String placeholder) {
        int leftBracket = placeholder.indexOf('(');
        if (leftBracket == -1) {
            return null;
        }
        int rightBracket = placeholder.lastIndexOf(')');
        if (rightBracket == -1) {
            return null;
        }
        if (leftBracket + 1 == rightBracket) {
            return null;
        }
        return placeholder.substring(leftBracket + 1, rightBracket).trim();
    }

    private String highlightMenuForSource(String sourceFileName) {
        return parameters.get(ServiceFacade.PATH).contains(sourceFileName) ? "class=\"active\"" : "";
    }

    private String selectPeriodForFilter(String value) {
        return ServiceFacade.INSTANCE.isDateFilterEqual(this, value) ? "selected=\"selected\"" : "";
    }

    private String setFilemaskActive(String sourceFileName) {
        return parameters.get(ServiceFacade.PATH).contains(sourceFileName) ? "true" : "false";
    }

    private String getPath(String ignored) {
        return parameters.get(ServiceFacade.PATH);
    }

    private enum Functions {
        highlight_menu_for(TemplateProcessor::highlightMenuForSource),
        cost_analysis_files(ServiceFacade.INSTANCE::getFileStatisticsForPlot),
        cost_analysis_modules(ServiceFacade.INSTANCE::getModuleStatisticsForPlot),
        merge_analysis_modules(ServiceFacade.INSTANCE::getSharedModulesForBar),
        map_location_data(ServiceFacade.INSTANCE::getContributionsByLocation),
        chart_team_data(ServiceFacade.INSTANCE::getContributionsByTeam),
        get_age_separator(ServiceFacade.INSTANCE::getAgeSeparator),
        get_age_comparison(ServiceFacade.INSTANCE::getAgeComparison),
        project_activity(ServiceFacade.INSTANCE::getProjectActivity),
        implicit_dependencies(ServiceFacade.INSTANCE::getImplicitDependencies),
        select_period_for(TemplateProcessor::selectPeriodForFilter),
        set_filemask_for(TemplateProcessor::setFilemaskActive),
        filemask_value(ServiceFacade.INSTANCE::getFileMask),
        return_page(TemplateProcessor::getPath);

        private BiFunction<TemplateProcessor, String, String> transformation;

        Functions(BiFunction<TemplateProcessor, String, String> transformation) {
            this.transformation = transformation;
        }
    }
}
