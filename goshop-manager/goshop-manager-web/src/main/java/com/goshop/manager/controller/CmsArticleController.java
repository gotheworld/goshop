package com.goshop.manager.controller;

import com.github.pagehelper.PageInfo;
import com.goshop.common.attachment.AttachmentService;
import com.goshop.common.exception.PageException;
import com.goshop.common.utils.IDUtils;
import com.goshop.common.utils.JsonUtils;
import com.goshop.common.utils.ResponseMessageUtils;
import com.goshop.manager.i.CmsArticleClassService;
import com.goshop.manager.i.CmsArticleService;
import com.goshop.manager.pojo.CmsArticle;
import com.goshop.manager.pojo.CmsArticleClass;
import com.goshop.manager.pojo.CmsArticleWithBLOBs;
import com.goshop.manager.pojo.User;
import com.goshop.manager.utils.Jump;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cms_article")
public class CmsArticleController {

    @Autowired
    CmsArticleService cmsArticleService;

    @Autowired
    CmsArticleClassService cmsArticleClassService;

    @Autowired
    AttachmentService attachmentService;

    @RequestMapping(value = "cms_article_list",method = RequestMethod.GET)
    public String index(@RequestParam(value="p",required=false) Integer curPage,
                        Model model, HttpServletRequest request) {
        PageInfo<CmsArticle> page=cmsArticleService.findBaseByArticleState(curPage, 20, CmsArticleService.TYPE_PUBLISH);
        model.addAttribute("P_PAGE",page);
        return "cms/article";
    }

    @RequestMapping(value = "add",method = RequestMethod.GET)
    public String addPage(Model model, HttpServletRequest request) {
        List<CmsArticleClass> classList =cmsArticleClassService.findTreeByParentId(null);
        model.addAttribute("P_CLASS_LIST",classList);
        return "cms/article_add";
    }


    @RequestMapping(value = "add",method = RequestMethod.POST)
    public String add(User user,CmsArticleWithBLOBs cmsArticle,Model model, HttpServletRequest request) {
        String url=request.getContextPath()+"/cms_article/cms_article_list";
        //管理员投稿
        cmsArticle.setArticleType(1);
        cmsArticle.setArticlePublisherId(user.getId());
        cmsArticle.setArticlePublisherName(user.getLoginName());
        cmsArticleService.save(cmsArticle);
        return Jump.get(url, "保存成功！");
    }

    @RequestMapping("/article_pic_upload")
    @ResponseBody
    public void articlePicUpload(@RequestParam(value = "fileupload") MultipartFile fileUpload,
                                   HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> retMap = new HashMap<>();
        try {
            String path=attachmentService.upload(fileUpload);
            retMap.put("file_id", IDUtils.getUuid());
            retMap.put("file_name",path);
            retMap.put("file_path",path);
        } catch (IOException e) {
            e.printStackTrace();
            throw new PageException("文件上传错误！");
        }
        ResponseMessageUtils.textPlainResponse(response, JsonUtils.objectToJson(retMap));
    }

    @RequestMapping("delete")
    public String delete(Long article_id,Model model, HttpServletRequest request) {
        String url=request.getContextPath()+"/cms_article/cms_article_list";
        cmsArticleService.delete(article_id);
        return Jump.get(url, "删除成功！");
    }
}
