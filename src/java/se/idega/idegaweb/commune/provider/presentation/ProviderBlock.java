/*
 * Created on 13.10.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package se.idega.idegaweb.commune.provider.presentation;

import java.rmi.RemoteException;

import se.idega.idegaweb.commune.business.CommuneUserBusiness;
import se.idega.idegaweb.commune.presentation.CommuneBlock;
import se.idega.idegaweb.commune.provider.business.ProviderSession;
import se.idega.idegaweb.commune.provider.event.ProviderEventListener;

import com.idega.block.school.business.SchoolBusiness;
import com.idega.business.IBOLookup;
import com.idega.business.IBORuntimeException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWUserContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.util.SelectorUtility;

/**
 * @author laddi
 */
public abstract class ProviderBlock extends CommuneBlock {

	public final static String IW_PROVIDER_BUNDLE_IDENTIFER = "se.idega.idegaweb.commune.provider";
	
	private ProviderSession _session;
	private SchoolBusiness _schoolBusiness;

	public void main(IWContext iwc) throws Exception{
		setResourceBundle(getResourceBundle(iwc));
		_session = getProviderSession(iwc);
		_schoolBusiness = getSchoolBusiness(iwc);

		init(iwc);
	}
	
	public abstract void init(IWContext iwc) throws Exception;
	
	/* (non-Javadoc)
	 * @see com.idega.presentation.PresentationObject#getBundleIdentifier()
	 */
	public String getBundleIdentifier() {
		return IW_PROVIDER_BUNDLE_IDENTIFER;
	}

	protected CommuneUserBusiness getUserBusiness(IWApplicationContext iwac) {
		try {
			return (CommuneUserBusiness) IBOLookup.getServiceInstance(iwac, CommuneUserBusiness.class);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re.getMessage());
		}
	}

	protected SchoolBusiness getSchoolBusiness(IWApplicationContext iwac) {
		try {
			return (SchoolBusiness) IBOLookup.getServiceInstance(iwac, SchoolBusiness.class);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re.getMessage());
		}
	}
	
	protected ProviderSession getProviderSession(IWUserContext iwuc) {
		try {
			return (ProviderSession) IBOLookup.getSessionInstance(iwuc, ProviderSession.class);
		}
		catch (RemoteException re) {
			throw new IBORuntimeException(re.getMessage());
		}
	}
	
	/**
	 * @return Returns the session.
	 */
	protected ProviderSession getSession() {
		return this._session;
	}

	/**
	 * @return Returns the schoolBusiness.
	 */
	protected SchoolBusiness getSchoolBusiness() {
		return this._schoolBusiness;
	}

	protected Form getNavigationForm() throws RemoteException {
		Form form = new Form();
		form.setEventListener(ProviderEventListener.class);
		
		Table table = new Table(5,1);
		table.setCellpadding(0);
		table.setCellspacing(0);
		table.setWidth(3, 8);
		form.add(table);
		int row = 1;
		
		SelectorUtility selector = new SelectorUtility();

		table.add(getSmallHeader(localize("school.season","Season")+":"+Text.NON_BREAKING_SPACE),1,row);
		DropdownMenu seasons = (DropdownMenu) getStyledInterface(selector.getSelectorFromIDOEntities(new DropdownMenu(getSession().getParameterSeasonID()), getSchoolBusiness().findAllSchoolSeasons(), "getSchoolSeasonName"));
		seasons.addMenuElementFirst("-1","");
		seasons.setToSubmit();
		if (getSession().getSeasonID() != -1)
			seasons.setSelectedElement(getSession().getSeasonID());
		table.add(seasons, 2, row);
		
		table.add(getSmallHeader(localize("school.year","Year")+":"+Text.NON_BREAKING_SPACE),4,row);
		DropdownMenu years = (DropdownMenu) getStyledInterface(selector.getSelectorFromIDOEntities(new DropdownMenu(getSession().getParameterYearID()), getSchoolBusiness().findAllSchoolYearsInSchool(getSession().getProviderID()), "getSchoolYearName"));
		years.addMenuElementFirst("-1","");
		years.setToSubmit();
		if (getSession().getYearID() != -1)
			years.setSelectedElement(getSession().getYearID());
		table.add(years,5,row);
		
		return form;
	}
}