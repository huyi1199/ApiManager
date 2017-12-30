package cn.crap.service.tool;

import cn.crap.dao.custom.CustomArticleDao;
import cn.crap.dto.LoginInfoDto;
import cn.crap.dto.PickDto;
import cn.crap.enumer.LuceneSearchType;
import cn.crap.enumer.ProjectType;
import cn.crap.framework.MyException;
import cn.crap.model.mybatis.*;
import cn.crap.service.IPickService;
import cn.crap.service.custom.CustomModuleService;
import cn.crap.service.custom.CustomProjectService;
import cn.crap.service.mybatis.ModuleService;
import cn.crap.service.mybatis.ProjectService;
import cn.crap.service.mybatis.UserService;
import cn.crap.utils.IConst;
import cn.crap.utils.LoginUserHelper;
import cn.crap.utils.MyString;
import cn.crap.utils.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * 下拉选着
 * @author Ehsan
 *
 */
@Service("userPickService")
public class UserPickService implements IPickService{
	@Autowired
	private ProjectService projectService;
	@Autowired
	private CustomProjectService customProjectService;
	@Autowired
	private ModuleService moduleService;
	@Autowired
	private CustomArticleDao customArticleMapper;
	@Autowired
	private UserService userService;
	@Autowired
	private CustomModuleService customModuleService;

	@Override
	public void getPickList(List<PickDto> picks, String code, String key, LoginInfoDto user) throws MyException {
		// 需要登陆才能
				PickDto pick = null;
				List<String> projectIds = null;
				switch (code) {
					case "CATEGORY":
					int i = 0;
					List<String> categorys = customArticleMapper.queryArticleCategoryByUserId(user.getId());
					for (String w : categorys) {
						if (w == null)
							continue;
						i++;
						pick = new PickDto("cat_" + i, w, w);
						picks.add(pick);
					}
					return;
//					case "MYPROJECT":// 用户所有项目
//						// 普通用户，只能查看自己的模块
//						for (Project p : projectService.findByMap(Tools.getMap("userId", Tools.getUser().getId()), null, null)) {
//							pick = new PickDto(p.getId(), p.getName());
//							picks.add(pick);
//						}
//						return;
					case "MYMODULE":// 用户所有模块
						// 普通用户，只能查看自己的模块
						ProjectCriteria example = new ProjectCriteria();
						ProjectCriteria.Criteria criteria = example.createCriteria().andUserIdEqualTo(LoginUserHelper.getUser().getId()).andStatusGreaterThan(Byte.valueOf("0"));
						for (Project p : projectService.selectByExample(example)) {
							pick = new PickDto(IConst.SEPARATOR, p.getName());
							picks.add(pick);

							ModuleCriteria moduleExample = new ModuleCriteria();
							moduleExample.createCriteria().andProjectIdEqualTo(p.getId());
							
							for(Module m : moduleService.selectByExample(moduleExample)){
								pick = new PickDto(m.getId(), m.getName());
								picks.add(pick);
							}
						}
						return;
					case "PROJECT_MODULE":
						// 普通用户，只能查看自己的项目和模块
						projectIds = customProjectService.queryProjectIdByUid(LoginUserHelper.getUser().getId());
						customModuleService.getDataCenterPick(picks, projectIds , "", "", "");
						return;
					case "MODULES":
						// 查看某个项目下的模块
						if(!MyString.isEmpty(key)){
							for(Module m : customModuleService.queryByProjectId(key)){
								pick = new PickDto(m.getId(), m.getName());
								picks.add(pick);
							}
						}
						return;
					case "PROJECTTYPE":
						for (ProjectType pt : ProjectType.values()) {
							pick = new PickDto(pt.getType()+"", pt.getName());
							picks.add(pick);
						}
						return;
					case "LUCENESEARCHTYPE":
						for (LuceneSearchType lc : LuceneSearchType.values()) {
							pick = new PickDto( lc.getValue() + "" , lc.getName());
							picks.add(pick);
						}
						return;
					case "USER":
						if(key!= null && key.length()>=4){
							Set<String> userIds = new TreeSet<String>();

							UserCriteria userExample = new UserCriteria();
							userExample.createCriteria().andEmailLike(key);

							for (User u : userService.selectByExample(userExample)) {
								if( !userIds.contains(u.getId()) ){
									pick = new PickDto(u.getId(), u.getUserName());
									picks.add(pick);
									userIds.add(u.getId());
								}
							}
							
							if(key.indexOf("@")<0){
								 userExample = new UserCriteria();
								userExample.createCriteria().andUserNameLike(key);
								for (User u : userService.selectByExample(userExample)) {
									if( !userIds.contains(u.getId()) ){
										pick = new PickDto(u.getId(), u.getUserName());
										picks.add(pick);
										userIds.add(u.getId());
									}
								}
							}
						}else{
							pick = new PickDto(IConst.SEPARATOR, "输入的搜索长度必须大于3");
							picks.add(pick);
						}
				}
			}

}
