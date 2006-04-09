/*
 * Created on 13.10.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package se.idega.idegaweb.commune.provider.presentation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.FinderException;
import se.idega.idegaweb.commune.school.business.SchoolCommuneSessionBean;
import com.idega.block.navigation.presentation.UserHomeLink;
import com.idega.block.school.business.SchoolYearComparator;
import com.idega.block.school.data.School;
import com.idega.block.school.data.SchoolClass;
import com.idega.block.school.data.SchoolStudyPath;
import com.idega.block.school.data.SchoolYear;
import com.idega.block.school.presentation.SchoolUserChooser;
import com.idega.business.IBORuntimeException;
import com.idega.data.IDORelationshipException;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Break;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.CloseButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.Parameter;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.ui.util.SelectorUtility;
import com.idega.user.data.User;

/**
 * @author laddi
 * Known subclasses: SchoolGroupEditorAdmin
 */
public class SchoolGroupEditor extends ProviderBlock {

	public final String PARAMETER_ACTION = "sge_action";	
	private final String PARAMETER_GROUP_ID = "sge_group_id";
	private final String PARAMETER_GROUP_STRING_ID = "sge_group_str_id";
	private final String PARAMETER_GROUP_NAME ="sge_group_name";
	private final String PARAMETER_SCHOOL_YEARS ="sge_school_years";
	private final String PARAMETER_TEACHERS ="sge_teachers";
	private final String PARAMETER_STUDY_PATHS ="sge_study_paths";
	protected final String PARAMETER_SEASON_ID ="sge_season_id";
	public final static String PARAMETER_TYPE_ID ="sge_type_id";
	private final String PARAMETER_IS_SUBGROUP = "sge_is_subgroup";
	
	public static final int ACTION_CLOSE = 0;
	public static final int ACTION_VIEW = 1;
	protected static final int ACTION_EDIT = 2;
	private static final int ACTION_DELETE = 3;
	private static final int ACTION_SAVE = 4;
	
	private int _action = ACTION_VIEW;
	private int _groupID = -1;
	private SchoolClass _group;
	protected School _provider;
	
	private CloseButton cancel;
	
	private boolean showStudyPaths = false;
	private boolean useStyleNames = false;
	private boolean useGroupStringId = false;

	//public final String PARAMETER_PROVIDER_ID = "Goran please fix this, ACTION_VIEW and PARAMETER_ACTION";
	
	public boolean getUseGroupStringId() {
		return this.useGroupStringId;
	}

	public void setUseGroupStringId(boolean b) {
		this.useGroupStringId = b;
	}
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#_main(com.idega.presentation.IWContext)
	 */
	public void init(IWContext iwc) throws Exception {
		try {
			this._provider = getProvider();
			parseAction(iwc);
			

			switch (this._action) {
				case ACTION_VIEW :
					add(getOverview());
					break;
				case ACTION_EDIT :
					add(getEditForm());
					break;
				case ACTION_DELETE :
					deleteGroup();
					add(getOverview());
					break;
				case ACTION_SAVE :
					saveGroup(iwc);
					add(getOverview());
					break;
			} 

	
		}
		catch (FinderException e) {
			add(getSmallErrorText(localize("school.no_provider_found", "No provider found for this user...")));
			add(new Break(2));
			add(new UserHomeLink());
		}
	}
	
	protected School getProvider() throws RemoteException, FinderException{
		return getSession().getProvider();
	}
	

	
	protected Table getOverview() throws RemoteException {
		Table table = new Table(1, 5);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setHeight(2, 12);
		table.setHeight(4, 12);
		table.setWidth(getWidth());
		
		table.add(getNavigationForm(this.showStudyPaths), 1, 1);
		
		if (this._provider != null){
			table.add(getGroupTable(), 1, 3);
			
			GenericButton button = getButton(new GenericButton("edit", localize("new_group", "New group")));
			button.setPageToOpen(getParentPageID());
			button.addParameterToPage(this.PARAMETER_ACTION, ACTION_EDIT);
			Parameter providerPar = getProviderAsParameter();
			button.addParameterToPage(providerPar.getName(), providerPar.getValueAsString());
			table.setCellpaddingLeft(1, 5, 12);
			table.add(button, 1, 5);
		
		}
		return table;
	}
	
	protected Table getGroupTable() {
		Table table = new Table();
		table.setCellpadding(getCellpadding());
		table.setCellspacing(getCellspacing());
		table.setWidth(getWidth());
		table.setColumns(7);
		table.setWidth(6, 12);
		table.setWidth(7, 12);
		int row = 1;
		int column = 1;
		boolean canDelete = true;
		
		table.add(getLocalizedSmallHeader("group_name","Name"), column++, row);
		if (!this.showStudyPaths) {
			table.add(getLocalizedSmallHeader("group_type","Type"), column++, row);
		}
		table.add(getLocalizedSmallHeader("school_season","Season"), column++, row);
		table.add(getLocalizedSmallHeader("school_years","Years"), column++, row);
		table.add(getLocalizedSmallHeader("teachers","Teachers"), column++, row);
		if (this.showStudyPaths) {
			table.add(getLocalizedSmallHeader("study_paths","Study paths"), column++, row);
		}
		if (this.useStyleNames) {
			table.setRowStyleClass(row, getHeaderRow2Class());
			table.setCellpaddingLeft(1, row++, 12);
		}
		else {
			table.setRowColor(row++, getHeaderColor());
		}
		Collection studyPaths = null;
		
		Iterator iter = getSchoolGroups().iterator();
		while (iter.hasNext()) {
			column = 1;
			SchoolClass group = (SchoolClass) iter.next();
			if (this.showStudyPaths) {
				try {
					studyPaths = group.findRelatedStudyPaths();
				}
				catch (IDORelationshipException ile) {
					studyPaths = new ArrayList();
				}
			}
			try {
				if (getSchoolBusiness().getNumberOfStudentsInClass(((Integer)group.getPrimaryKey()).intValue()) > 0) {
					canDelete = false;
				}
				else {
					canDelete = true;
				}
			}
			catch (RemoteException e1) {
				e1.printStackTrace();
			}
			catch (EJBException e1) {
				e1.printStackTrace();
			}
				
			if (this.useStyleNames) {
				table.setCellpaddingLeft(1, row, 12);
				if (row % 2 == 0) {
					table.setRowStyleClass(row, getDarkRowClass());
				}
				else {
					table.setRowStyleClass(row, getLightRowClass());
				}
			}
			else {
				if (row % 2 == 0) {
					table.setRowColor(row, getZebraColor1());
				}
				else {
					table.setRowColor(row, getZebraColor2());
				}
			}

			table.add(getSmallText(group.getSchoolClassName()), column++, row);
			if (!this.showStudyPaths) {
				if (group.getSchoolTypeId() != -1) {
					table.add(getSmallText(group.getSchoolType().getSchoolTypeName()), column++, row);
				}
				else {
					table.add(getSmallText("-"), column++, row);
				}
			}
			if (group.getSchoolSeasonId() != -1) {
				table.add(getSmallText(group.getSchoolSeason().getSchoolSeasonName()), column++, row);
			}
			else {
				table.add(getSmallText("-"), column++, row);
			}
			
			List years = null;
			try {
				years = new ArrayList(group.findRelatedSchoolYears());
			}
			catch (IDORelationshipException e) {
				years = new ArrayList();
			}
			
			if (!years.isEmpty()) {
				Collections.sort(years, new SchoolYearComparator());
				Iterator iterator = years.iterator();
				StringBuffer buffer = new StringBuffer();
				while (iterator.hasNext()) {
					SchoolYear year = (SchoolYear) iterator.next();
					buffer.append(year.getSchoolYearName());
					if (iterator.hasNext()) {
						buffer.append(",").append(Text.NON_BREAKING_SPACE);
					}
				}
				table.add(getSmallText(buffer.toString()), column++, row);
			}
			else {
				table.add(getSmallText("-"), column++, row);
			}

			Collection teachers = null;
			try {
				teachers = new ArrayList(group.findRelatedUsers());
			}
			catch (IDORelationshipException e) {
				teachers = new ArrayList();
			}

			if (!teachers.isEmpty()) {
				Iterator iterator = teachers.iterator();
				StringBuffer buffer = new StringBuffer();
				while (iterator.hasNext()) {
					User teacher = (User) iterator.next();
					buffer.append(teacher.getLastName());
					if (iterator.hasNext()) {
						buffer.append(",").append(Text.NON_BREAKING_SPACE);
					}
				}
				table.add(getSmallText(buffer.toString()), column++, row);
			}
			else {
				table.add(getSmallText("-"), column++, row);
			}
			
			if (this.showStudyPaths) {
				if (!studyPaths.isEmpty()) {
					Iterator iterator = studyPaths.iterator();
					StringBuffer buffer = new StringBuffer();
					while (iterator.hasNext()) {
						SchoolStudyPath studyPath = (SchoolStudyPath) iterator.next();
						buffer.append(localize(studyPath.getCode(), studyPath.getDescription()));
						if (iterator.hasNext()) {
							buffer.append(",").append(Text.NON_BREAKING_SPACE);
						}
					}
					table.add(getSmallText(buffer.toString()), column++, row);
				}
				else {
					table.add(getSmallText("-"), column++, row);
				}
			}
			
			Link editLink = new Link(this.getEditIcon(localize("edit_group", "Edit group")));
			editLink.addParameter(this.PARAMETER_ACTION, ACTION_EDIT);
			editLink.addParameter(this.PARAMETER_GROUP_ID, group.getPrimaryKey().toString());
			editLink.addParameter(getProviderAsParameter());
			table.add(editLink, column++, row);

			Link deleteLink = new Link(this.getDeleteIcon(localize("delete_group", "Delete group")));
			if (canDelete) {
				deleteLink.addParameter(this.PARAMETER_ACTION, ACTION_DELETE);
				deleteLink.addParameter(this.PARAMETER_GROUP_ID, group.getPrimaryKey().toString());
				deleteLink.addParameter(getProviderAsParameter());
			}
			else {
				deleteLink.setToOpenAlert(localize("delete_group.can_not_delete_group", "The group can not be deleted as it contains students."));
			}
			table.add(deleteLink, column++, row++);
		}
		
		return table;
	}
	
	Parameter getProviderAsParameter(){
		return new Parameter("", "");
	}
	
	protected Form getEditForm() {
		Form form = new Form();
		form.addParameter(this.PARAMETER_GROUP_ID, this._groupID);
		form.addParameter(this.PARAMETER_ACTION, -1);
		form.maintainParameter(SchoolCommuneSessionBean.PARAMETER_SCHOOL_ID);
		
		Table table = new Table();
		table.setCellpadding(2);
		table.setCellspacing(0);
		table.setWidth(getWidth());
		table.setColumns(3);
		table.setWidth(2, 10);
		table.setWidth(3, Table.HUNDRED_PERCENT);
		form.add(table);
		
		int row = 1;
		SelectorUtility util = new SelectorUtility();
		
		if (this.useGroupStringId) {
			table.add(getSmallHeader(localize("group_string_id", "Group ID") + ":"), 1, row);			
			table.setNoWrap(1, row);
			TextInput groupStringId = (TextInput) getStyledInterface(new TextInput(this.PARAMETER_GROUP_STRING_ID));
			if (this._group != null && this._group.getGroupStringId() != null) {
				groupStringId.setContent(this._group.getGroupStringId());
			}
			if (this.useStyleNames) {
				table.setCellpaddingLeft(1, row, 12);
			}
			groupStringId.setAsNotEmpty(localize("group_id_not_empty", "Group ID must be entered."));
			table.add(groupStringId, 3, row++);
		}
		table.add(getSmallHeader(localize("group_name", "Name") + ":"), 1, row);
		table.setNoWrap(1, row);
		TextInput name = (TextInput) getStyledInterface(new TextInput(this.PARAMETER_GROUP_NAME));
		name.setAsNotEmpty(localize("must_supply_group_name", "You must enter a group name."));
			
		if (this._group != null && this._group.getSchoolClassName() != null) {
			name.setContent(this._group.getSchoolClassName());
		}
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		table.add(name, 3, row++);
		
		table.add(getSmallHeader(localize("group_type", "Type") + ":"), 1, row);
		table.setNoWrap(1, row);
		Collection providerTypes = null;
		try {
			providerTypes = this._provider.findRelatedSchoolTypes();
		}
		catch (IDORelationshipException e) {
			providerTypes = new ArrayList();
		}
		DropdownMenu types = (DropdownMenu) getStyledInterface(util.getSelectorFromIDOEntities(new DropdownMenu(PARAMETER_TYPE_ID), providerTypes, "getSchoolTypeName"));
		types.addMenuElementFirst("-1", "");
		setSelectedSchoolType(types);
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		table.add(types, 3, row++);
		
		table.add(getSmallHeader(localize("school_season", "Season") + ":"), 1, row);
		table.setNoWrap(1, row);
		Collection providerSeasons = null; 
		try {
			providerSeasons = getSchoolBusiness().findAllSchoolSeasons(getSchoolBusiness().getCategoryElementarySchool());
		}
		catch (RemoteException e) {
			providerSeasons = new ArrayList();
		}
		DropdownMenu seasons = (DropdownMenu) getStyledInterface(util.getSelectorFromIDOEntities(new DropdownMenu(this.PARAMETER_SEASON_ID), providerSeasons, "getSchoolSeasonName"));
		seasons.addMenuElementFirst("-1", "");
		setSelectedSeason(seasons);
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		table.add(seasons, 3, row++);
		
		table.add(getSmallHeader(localize("group_type", "Group type") + ":"), 1, row);
		table.setNoWrap(1, row);
		DropdownMenu subGroup = (DropdownMenu) getStyledInterface(new DropdownMenu(this.PARAMETER_IS_SUBGROUP));
		subGroup.addMenuElement("false", localize("main_group", "Main group"));
		subGroup.addMenuElement("true", localize("sub_group", "Sub group"));
		if (this._group != null) {
			subGroup.setSelectedElement(String.valueOf(this._group.getIsSubGroup()));
		}
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		table.add(subGroup, 3, row++);
		
		List schoolYears = null;
		try {
			schoolYears = new ArrayList(this._provider.findRelatedSchoolYears());
		}
		catch (IDORelationshipException e1) {
			schoolYears = new ArrayList();
		}
		if (!schoolYears.isEmpty()) {
			Collections.sort(schoolYears, new SchoolYearComparator());
		}
		
		Collection groupYears = new ArrayList();
		if (this._group != null) {
			try {
				groupYears = this._group.findRelatedSchoolYears();
			}
			catch (IDORelationshipException e2) {
				groupYears = new ArrayList();
			}
		}
				
		table.setHeight(row++, 15);
		table.add(getSmallHeader(localize("school_years", "Years") + ":"), 1, row);
		table.setNoWrap(1, row);
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		
		Iterator iter = schoolYears.iterator();
		while (iter.hasNext()) {
			SchoolYear year = (SchoolYear) iter.next();
			CheckBox box = getCheckBox(this.PARAMETER_SCHOOL_YEARS, year.getPrimaryKey().toString());
			if (groupYears.contains(year)) {
				box.setChecked(true);
			}
			
			table.setCellpadding(3, row, 2);
			table.add(box, 3, row);
			table.add(Text.getNonBrakingSpace(), 3, row);
			table.add(getSmallText(localize(year.getSchoolYearName(), year.getSchoolYearName())), 3, row++);
		}
		table.setHeight(row++, 15);
		
		//Study paths
		if (this.showStudyPaths) {
			List paths = null;
			try {
				paths = new ArrayList(this._provider.findRelatedStudyPaths());
			}
			catch (IDORelationshipException e1) {
				paths = new ArrayList();
			}
			
			Collection studyPaths = new ArrayList();
			if (this._group != null) {
				try {
					studyPaths = this._group.findRelatedStudyPaths();
				}
				catch (IDORelationshipException e2) {
					studyPaths = new ArrayList();
				}
			}
					
			table.setHeight(row++, 15);
			table.add(getSmallHeader(localize("study_paths", "Study paths") + ":"), 1, row);
			table.setNoWrap(1, row);
			if (this.useStyleNames) {
				table.setCellpaddingLeft(1, row, 12);
			}
			
			iter = paths.iterator();
			while (iter.hasNext()) {
				SchoolStudyPath path = (SchoolStudyPath) iter.next();
				CheckBox box = getCheckBox(this.PARAMETER_STUDY_PATHS, path.getPrimaryKey().toString());
				if (studyPaths.contains(path)) {
					box.setChecked(true);
				}
				
				table.setCellpadding(3, row, 2);
				table.add(box, 3, row);
				table.add(Text.getNonBrakingSpace(), 3, row);
				table.add(getSmallText(localize(path.getCode(), path.getDescription())), 3, row++);
			}
			table.setHeight(row++, 15);
		}
		//study paths...
		
		Link link = new Link(localize("create_school_user", "Create school user"));
		link.setAsImageButton(true);
		link.setWindowToOpen(com.idega.block.school.presentation.SchoolUserWindow.class);
		link.setParameter("sue_act", "sue_pvs");
		link.setParameter("pr_schl_id", this._provider.getPrimaryKey().toString());
		
		table.add(link, 3, row++);

		List groupTeachers = new ArrayList();
		if (this._group != null) {
			try {
				groupTeachers = new ArrayList(this._group.findRelatedUsers());
			}
			catch (IDORelationshipException e2) {
				groupTeachers = new ArrayList();
			}
		}
		
		SchoolUserChooser chooser;
		int size = groupTeachers.size();
		for (int a = 0; a < 4; a++) {
			if (a == 0) {
				table.add(getSmallHeader(localize("teacher", "Teacher") + ":"), 1, row);
				table.setNoWrap(1, row);
				if (this.useStyleNames) {
					table.setCellpaddingLeft(1, row, 12);
				}
			}
			chooser = new SchoolUserChooser(this.PARAMETER_TEACHERS+"_"+(a+1), this._provider);

			if (a < size) {
				User teacher = (User) groupTeachers.get(a);
				chooser.setSelected(teacher);
			}
			table.add(chooser, 3, row++);
			
			if ((a + 1) < 4) {
				table.setHeight(row++, 3);
			}
		}
				
		table.setHeight(row++, 12);
		if (this.useStyleNames) {
			table.setCellpaddingLeft(1, row, 12);
		}
		table.mergeCells(1, row, 3, row);
		SubmitButton save = (SubmitButton) getButton(new SubmitButton(localize("save_group", "Save group")));
		save.setValueOnClick(this.PARAMETER_ACTION, String.valueOf(ACTION_SAVE));
		table.add(save, 1, row);
		table.add(Text.getNonBrakingSpace(), 1, row);
		//ac okt 2005
		this.cancel = (CloseButton) getStyledInterface(new CloseButton(localize("close_window", "Close")));
		this.cancel.setPageToOpen(getParentPageID());
		this.cancel.setValueOnClick(this.PARAMETER_ACTION, String.valueOf(ACTION_CLOSE));
		table.add(this.cancel, 1, row);
		
		return form;
	}

	protected void setSelectedSchoolType(DropdownMenu types) {
		if (this._group != null && this._group.getSchoolTypeId() != -1) {
			types.setSelectedElement(this._group.getSchoolTypeId());
		}
	}

	protected void setSelectedSeason(DropdownMenu seasons) {
		if (this._group != null && this._group.getSchoolSeasonId() != -1){
			seasons.setSelectedElement(this._group.getSchoolSeasonId());
			seasons.setDisabled(true);
		}
	}
	
	private void deleteGroup() {
		try {
			getSchoolBusiness().removeSchoolClass(this._groupID);
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private void saveGroup(IWContext iwc) {
		String groupStringId = iwc.getParameter(this.PARAMETER_GROUP_STRING_ID);
		String name = iwc.getParameter(this.PARAMETER_GROUP_NAME);
		if (name.trim().length() == 0) {
			getParentPage().setAlertOnLoad(localize("empty_name_value", "Can not store group with empty name value"));
			return;
		}
		String[] years = iwc.getParameterValues(this.PARAMETER_SCHOOL_YEARS);
		String[] studyPaths = iwc.getParameterValues(this.PARAMETER_STUDY_PATHS);
		String[] teachers = new String[4];
		int typeID = -1;
		if (iwc.isParameterSet(PARAMETER_TYPE_ID)) {
			typeID = Integer.parseInt(iwc.getParameter(PARAMETER_TYPE_ID));
		}
		int seasonID = -1;
		if (iwc.isParameterSet(this.PARAMETER_SEASON_ID)) {
			seasonID = Integer.parseInt(iwc.getParameter(this.PARAMETER_SEASON_ID));
		}
		boolean isSubGroup = Boolean.valueOf(iwc.getParameter(this.PARAMETER_IS_SUBGROUP)).booleanValue();
		
		for (int a = 1; a <= 4; a++) {
			String teacher = "-1";
			if (iwc.isParameterSet(this.PARAMETER_TEACHERS+"_"+a)) {
				teacher = iwc.getParameter(this.PARAMETER_TEACHERS+"_"+a);
			}
			teachers[a-1] = teacher;
		}
		
		try {
			SchoolClass schoolClass = getSchoolBusiness().storeSchoolClass(this._groupID, name, getProviderID(), typeID, seasonID, years, teachers, studyPaths, groupStringId);
			schoolClass.setIsSubGroup(isSubGroup);
			schoolClass.store();
		}
		catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	private Collection getSchoolGroups() {
		try {
			return getSchoolBusiness().findSchoolClassesBySchoolAndSeasonAndYearAndStudyPath(getProviderID(), getSession().getSeasonID(), getSession().getYearID(), getSession().getStudyPathID());
		}
		catch (RemoteException e) {
			throw new IBORuntimeException(e.getMessage());
		}
	}
	
	private boolean multipleSchools = false;
	
	public void setMultipleSchools(boolean multiple) {
		this.multipleSchools = multiple;
	}
	
	public boolean getMultipleSchools() {
		return this.multipleSchools;
	}	

	/**
	 * @param iwc
	 */
	private void parseAction(IWContext iwc) {
		if (iwc.isParameterSet(this.PARAMETER_ACTION)) {
			this._action = Integer.parseInt(iwc.getParameter(this.PARAMETER_ACTION));
		}
		if (iwc.isParameterSet(this.PARAMETER_GROUP_ID)) {
			this._groupID = Integer.parseInt(iwc.getParameter(this.PARAMETER_GROUP_ID));
		}
		
		if (this._groupID != -1) {
			try {
				this._group = getSchoolBusiness().findSchoolClass(new Integer(this._groupID));
			}
			catch (RemoteException e) {
				this._group = null;
			}
		}
	}
	/**
	 * @param showStudyPaths The showStudyPaths to set.
	 */
	public void setShowStudyPaths(boolean showStudyPaths) {
		this.showStudyPaths = showStudyPaths;
	}
	/**
	 * @param useStyleNames The useStyleNames to set.
	 */
	public void setUseStyleNames(boolean useStyleNames) {
		this.useStyleNames = useStyleNames;
	}
}