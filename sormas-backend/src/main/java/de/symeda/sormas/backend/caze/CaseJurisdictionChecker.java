package de.symeda.sormas.backend.caze;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

import de.symeda.sormas.api.caze.CaseJurisdictionDto;
import de.symeda.sormas.api.utils.jurisdiction.CaseJurisdictionHelper;
import de.symeda.sormas.backend.user.User;
import de.symeda.sormas.backend.user.UserService;
import de.symeda.sormas.backend.util.JurisdictionHelper;

@Stateless(name = "CaseJurisdictionChecker")
@LocalBean
public class CaseJurisdictionChecker {

	@EJB
	private UserService userService;

	public Boolean isInJurisdiction(Case caze) {
		return isInJurisdiction(JurisdictionHelper.createCaseJurisdictionDto(caze));
	}

	public Boolean isInJurisdiction(CaseJurisdictionDto caseJurisdictionDto) {
		User user = userService.getCurrentUser();

		return CaseJurisdictionHelper.isInJurisdiction(userService::hasAnyRole, JurisdictionHelper.createUserJurisdiction(user), caseJurisdictionDto);
	}
}
