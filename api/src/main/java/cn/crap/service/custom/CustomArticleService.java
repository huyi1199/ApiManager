package cn.crap.service.custom;

import cn.crap.adapter.ArticleAdapter;
import cn.crap.dao.mybatis.ArticleDao;
import cn.crap.dao.custom.CustomArticleDao;
import cn.crap.dto.SearchDto;
import cn.crap.enumer.LogType;
import cn.crap.model.mybatis.*;
import cn.crap.model.mybatis.Module;
import cn.crap.model.mybatis.ArticleCriteria;
import cn.crap.service.ILuceneService;
import cn.crap.service.tool.ModuleCache;
import cn.crap.service.tool.ProjectCache;
import cn.crap.service.mybatis.LogService;
import cn.crap.utils.MyString;
import cn.crap.utils.Page;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;


@Service
public class CustomArticleService implements ILuceneService{
    @Autowired
    private ArticleDao dao;
    @Autowired
    private CustomArticleDao customArticleMapper;
    @Resource(name = "projectCache")
    protected ProjectCache projectCache;
    @Resource(name = "moduleCache")
    protected ModuleCache moduleCache;
    @Autowired
    private LogService logService;

    public int countByProjectId(String moduleId, String name, String type, String category) {
        Assert.notNull(moduleId, "moduleId can't be null");
        ArticleCriteria example = new ArticleCriteria();
        ArticleCriteria.Criteria criteria = example.createCriteria().andModuleIdEqualTo(moduleId);
        if (name != null) {
            criteria.andNameLike("%" + name + "%");
        }
        if (type != null) {
            criteria.andTypeLike("%" + type + "%");
        }
        if (category != null) {
            criteria.andCategoryLike("%" + category + "%");
        }
        return dao.countByExample(example);
    }

    public List<Article> queryArticle(String moduleId, String name, String type, String category, Page page) {
        Assert.notNull(moduleId, "moduleId can't be null");
        ArticleCriteria example = new ArticleCriteria();
        ArticleCriteria.Criteria criteria = example.createCriteria().andModuleIdEqualTo(moduleId);
        if (!StringUtils.isEmpty(name)){
            criteria.andNameLike("%" + name + "%");
        }
        if (!StringUtils.isEmpty(type)){
            criteria.andTypeEqualTo(type);
        }
        if (!StringUtils.isEmpty(category)){
            criteria.andCategoryEqualTo(category);
        }
        example.setLimitStart(page.getStart());
        example.setMaxResults(page.getSize());
        return dao.selectByExample(example);
    }

    public Project getProject(String moduleId) {
        if (!MyString.isEmpty(moduleId)) {
            Module module = moduleCache.get(moduleId);
            if (module != null){
                return projectCache.get(module.getProjectId());
            }
        }
        return new Project();
    }

    public String getProjectId(String moduleId) {
        if (!MyString.isEmpty(moduleId)) {
            Module module = moduleCache.get(moduleId);
            if (module != null)
                return module.getProjectId();
        }
        return "";
    }

    public String getModuleName(String moduleId){
        if(!MyString.isEmpty(moduleId)){
            return getModule(moduleId).getName();
        }
        return "";
    }

    private Module getModule(String moduleId){
        if(!MyString.isEmpty(moduleId)){
            Module module = moduleCache.get(moduleId);
            if(module!=null) {
                return module;
            }
        }
        return new Module();
    }


    /**
     * update article and add update log
     * @param model
     * @param modelName
     * @param remark
     */
    public void update(ArticleWithBLOBs model, String modelName, String remark) {
        Article dbModel = dao.selectByPrimaryKey(model.getId());
        if(MyString.isEmpty(remark)) {
            remark = model.getName();
        }
// TODO 提取代码
            Log log = new Log();
            log.setModelName(modelName);
            log.setRemark(remark);
            log.setType(LogType.UPDATE.name());
            log.setContent(JSONObject.fromObject(dbModel).toString());
            log.setModelClass(dbModel.getClass().getSimpleName());

        logService.insert(log);
        dao.updateByPrimaryKeyWithBLOBs(model);
    }

    public void delete(String id, String modelName, String remark){
        Assert.notNull(id);
        Article dbModel = dao.selectByPrimaryKey(id);
        if(MyString.isEmpty(remark)) {
            remark = dbModel.getName();
        }
        Log log = new Log();
        log.setModelName(modelName);
        log.setRemark(remark);
        log.setType(LogType.DELTET.name());
        log.setContent(JSONObject.fromObject(dbModel).toString());
        log.setModelClass(dbModel.getClass().getSimpleName());

       logService.insert(log);
        dao.deleteByPrimaryKey(dbModel.getId());
    }

    public List<SearchDto> getAll() {
        return ArticleAdapter.getSearchDto(dao.selectByExampleWithBLOBs(new ArticleCriteria()));
    }

    @Override
    public String getLuceneType() {
        return "文章&数据字典";
    }

    @Override
    public List<SearchDto> getAllByProjectId(String projectId) {
        ArticleCriteria example = new ArticleCriteria();
        example.createCriteria().andProjectIdEqualTo(projectId);
        return  ArticleAdapter.getSearchDto(dao.selectByExampleWithBLOBs(example));
    }

    public Integer countByModuleIdAndType(String moduleId, String type){
        Assert.notNull(moduleId);
        Assert.notNull(type);
        ArticleCriteria example = new ArticleCriteria();
        example.createCriteria().andModuleIdEqualTo(moduleId).andTypeEqualTo(type);
        return dao.countByExample(example);
    }

    public List<Article> queryByModuleIdAndType(String moduleId, String type){
        Assert.notNull(moduleId);
        Assert.notNull(type);
        ArticleCriteria example = new ArticleCriteria();
        example.createCriteria().andModuleIdEqualTo(moduleId).andTypeEqualTo(type);
        return dao.selectByExample(example);
    }

    /**
     * 查询前20个分类
     * @param moduleId
     * @param type
     * @return
     */
    public List<String> queryTop20Category(String moduleId, String type){
        return customArticleMapper.queryTop20Category(moduleId, type);
    }

    /**
     * 跟新点击量
     * @param id
     */
    public void updateClickById(String id){
        Assert.notNull(id);
        customArticleMapper.updateClickById(id);
    }

    public ArticleWithBLOBs selectByKey(String key){
        ArticleCriteria example = new ArticleCriteria();
        example.createCriteria().andMkeyEqualTo(key);
        List<ArticleWithBLOBs> models = dao.selectByExampleWithBLOBs(example);
        return  models.size() > 0 ? models.get(0) : null;
    }
}
