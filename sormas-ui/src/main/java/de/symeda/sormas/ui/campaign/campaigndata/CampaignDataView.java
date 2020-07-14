/*
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2020 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package de.symeda.sormas.ui.campaign.campaigndata;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.campaign.form.CampaignFormReferenceDto;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.campaign.AbstractCampaignView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CssStyles;
import org.vaadin.hene.popupbutton.PopupButton;

@SuppressWarnings("serial")
public class CampaignDataView extends AbstractCampaignView {

	public static final String VIEW_NAME = ROOT_VIEW_NAME + "/campaigndata";

	public CampaignDataView() {
		super(VIEW_NAME);

		VerticalLayout newFormLayout = new VerticalLayout();
		{
			newFormLayout.setSpacing(true);
			newFormLayout.setMargin(true);
			newFormLayout.addStyleName(CssStyles.LAYOUT_MINIMAL);
			newFormLayout.setWidth(350, Unit.PIXELS);

			PopupButton newFormButton = ButtonHelper.createIconPopupButton(Captions.actionNewForm, VaadinIcons.PLUS_CIRCLE, newFormLayout);
			newFormButton.setId("new-form");

			for (CampaignFormReferenceDto campaignForm : FacadeProvider.getCampaignFormFacade().getAllCampaignFormsAsReferences()) {
				Button campaignFormButton = ButtonHelper
					.createButton(campaignForm.toString(), e -> ControllerProvider.getCampaignController().createCampaignDataForm(campaignForm));
				campaignFormButton.setWidth(100, Unit.PERCENTAGE);
				newFormLayout.addComponent(campaignFormButton);
			}

			addHeaderComponent(newFormButton);
		}

		HorizontalLayout placeholder = new HorizontalLayout();
		placeholder.setSizeFull();
		addComponent(placeholder);
	}

}
