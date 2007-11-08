package example.ui;

import com.googlecode.scopeplugin.ScopeType;
import com.googlecode.scopeplugin.annotations.Begin;
import com.googlecode.scopeplugin.annotations.End;
import com.googlecode.scopeplugin.annotations.In;
import com.googlecode.scopeplugin.annotations.Out;
import com.opensymphony.xwork.ActionSupport;


/** */
public class SurveyAction extends ActionSupport {
	public static final String SCOPE_KEY = "SUVERY_ACTION_SURVEY_BEAN";
	
	@In (scope=ScopeType.SESSION, value=SCOPE_KEY)
	@Out (scope=ScopeType.SESSION, value=SCOPE_KEY)
	private SurveyBean surveyBean;
	
	@Begin
	public String edit() {
		surveyBean = new SurveyBean();
		return SUCCESS;
	}
	
	public String save() {
		return SUCCESS;
	}
	
	public SurveyBean getSurveyBean() {
		return surveyBean;
	}

	public void setSurveyBean(SurveyBean surveyBean) {
		this.surveyBean = surveyBean;
	}
}
