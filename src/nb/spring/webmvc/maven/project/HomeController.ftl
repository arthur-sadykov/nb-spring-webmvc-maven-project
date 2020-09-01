<#assign licenseFirst = "/*">
<#assign licensePrefix = " * ">
<#assign licenseLast = " */">
<#include "${project.licensePath}">

<#if package?? && package != "">
    package ${package};
</#if>

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author ${user}
 */
@Controller
public class HomeController {

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }
}
