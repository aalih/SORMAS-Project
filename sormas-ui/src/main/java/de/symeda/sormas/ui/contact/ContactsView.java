package de.symeda.sormas.ui.contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.I18nProperties;
import de.symeda.sormas.api.contact.ContactClassification;
import de.symeda.sormas.api.contact.ContactIndexDto;
import de.symeda.sormas.api.contact.ContactStatus;
import de.symeda.sormas.api.contact.FollowUpStatus;
import de.symeda.sormas.api.facility.FacilityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.caze.CaseController;
import de.symeda.sormas.ui.login.LoginHelper;
import de.symeda.sormas.ui.utils.AbstractView;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DownloadUtil;
import de.symeda.sormas.ui.utils.LayoutUtil;

/**
 * A view for performing create-read-update-delete operations on products.
 *
 * See also {@link CaseController} for fetching the data, the actual CRUD
 * operations and controlling the view based on events from outside.
 */
public class ContactsView extends AbstractView {

	private static final long serialVersionUID = -3533557348144005469L;

	public static final String VIEW_NAME = "contacts";

	public static final String SEARCH_FIELD = "searchField";

	private ContactGrid grid;    
	private VerticalLayout gridLayout;

	private HashMap<Button, String> statusButtons;
	private Button activeStatusButton;

	public ContactsView() {
		super(VIEW_NAME);

		grid = new ContactGrid();        
		gridLayout = new VerticalLayout();
		gridLayout.addComponent(createFilterBar());
		gridLayout.addComponent(createStatusFilterBar());
		gridLayout.addComponent(grid);
		gridLayout.setMargin(true);
		gridLayout.setSpacing(false);
		gridLayout.setSizeFull();
		gridLayout.setExpandRatio(grid, 1);
		gridLayout.setStyleName("crud-main-layout");
		grid.getContainer().addItemSetChangeListener(e -> {
			updateActiveStatusButtonCaption();
		});

		if (LoginHelper.hasUserRight(UserRight.CONTACT_EXPORT)) {
			Button exportButton = new Button("Export");
			exportButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
			exportButton.setIcon(FontAwesome.DOWNLOAD);

			StreamResource streamResource = DownloadUtil.createGridExportStreamResource(grid.getContainerDataSource(), new ArrayList<>(grid.getColumns()), "sormas_contacts", "sormas_contacts_" + DateHelper.formatDateForExport(new Date()) + ".csv");
			FileDownloader fileDownloader = new FileDownloader(streamResource);
			fileDownloader.extend(exportButton);

			addHeaderComponent(exportButton);
		}

		addComponent(gridLayout);
	}

	public HorizontalLayout createFilterBar() {
		HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.setSpacing(true);
		filterLayout.setSizeUndefined();

		ComboBox classificationFilter = new ComboBox();
		classificationFilter.setWidth(140, Unit.PIXELS);
		classificationFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CONTACT_CLASSIFICATION));
		classificationFilter.addItems((Object[]) ContactClassification.values());
		classificationFilter.addValueChangeListener(e -> {
			grid.setClassificationFilter((ContactClassification) e.getProperty().getValue());
		});
		filterLayout.addComponent(classificationFilter);

		ComboBox diseaseFilter = new ComboBox();
		diseaseFilter.setWidth(140, Unit.PIXELS);
		diseaseFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CASE_DISEASE));
		diseaseFilter.addItems((Object[])Disease.values());
		diseaseFilter.addValueChangeListener(e -> {
			grid.setDiseaseFilter(((Disease)e.getProperty().getValue()));
		});
		filterLayout.addComponent(diseaseFilter);

		UserDto user = LoginHelper.getCurrentUser();

		ComboBox regionFilter = new ComboBox();
		if (user.getRegion() == null) {
			regionFilter.setWidth(140, Unit.PIXELS);
			regionFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CASE_REGION_UUID));
			regionFilter.addItems(FacadeProvider.getRegionFacade().getAllAsReference());
			regionFilter.addValueChangeListener(e -> {
				RegionReferenceDto region = (RegionReferenceDto) e.getProperty().getValue();
				grid.setRegionFilter(region != null ? region.getUuid() : null);
			});
			filterLayout.addComponent(regionFilter);
		}

		ComboBox districtFilter = new ComboBox();
		districtFilter.setWidth(140, Unit.PIXELS);
		districtFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CASE_DISTRICT_UUID));
		districtFilter.setDescription("Select a district in the state");
		districtFilter.addValueChangeListener(e -> {
			DistrictReferenceDto district = (DistrictReferenceDto) e.getProperty().getValue();
			grid.setDistrictFilter(district != null ? district.getUuid() : null);
		});

		if (user.getRegion() != null) {
			districtFilter.addItems(FacadeProvider.getDistrictFacade().getAllByRegion(user.getRegion().getUuid()));
			districtFilter.setEnabled(true);
		} else {
			regionFilter.addValueChangeListener(e -> {
				RegionReferenceDto region = (RegionReferenceDto)e.getProperty().getValue();
				districtFilter.removeAllItems();
				if (region != null) {
					districtFilter.addItems(FacadeProvider.getDistrictFacade().getAllByRegion(region.getUuid()));
					districtFilter.setEnabled(true);
				} else {
					districtFilter.setEnabled(false);
				}
			});
			districtFilter.setEnabled(false);
		}
		filterLayout.addComponent(districtFilter);

		ComboBox facilityFilter = new ComboBox();
		facilityFilter.setWidth(140, Unit.PIXELS);
		facilityFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CASE_HEALTH_FACILITY_UUID));
		facilityFilter.setDescription("Select a facility in the LGA");
		facilityFilter.addValueChangeListener(e -> {
			FacilityReferenceDto facility = (FacilityReferenceDto) e.getProperty().getValue();
			grid.setHealthFacilityFilter(facility != null ? facility.getUuid() : null);
		});
		facilityFilter.setEnabled(false);
		filterLayout.addComponent(facilityFilter);

		districtFilter.addValueChangeListener(e-> {
			facilityFilter.removeAllItems();
			DistrictReferenceDto district = (DistrictReferenceDto)e.getProperty().getValue();
			if (district != null) {
				facilityFilter.addItems(FacadeProvider.getFacilityFacade().getHealthFacilitiesByDistrict(district, true));
				facilityFilter.setEnabled(true);
			} else {
				facilityFilter.setEnabled(false);
			}
		});

		ComboBox officerFilter = new ComboBox();
		officerFilter.setWidth(140, Unit.PIXELS);
		officerFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.CONTACT_OFFICER_UUID));
		if (user.getRegion() != null) {
			officerFilter.addItems(FacadeProvider.getUserFacade().getUsersByRegionAndRoles(user.getRegion(), UserRole.CONTACT_OFFICER));
		}
		officerFilter.addValueChangeListener(e -> {
			UserReferenceDto officer = (UserReferenceDto) e.getProperty().getValue();
			grid.setContactOfficerFilter(officer != null ? officer.getUuid() : null);
		});
		filterLayout.addComponent(officerFilter);

		ComboBox followUpStatusFilter = new ComboBox();
		followUpStatusFilter.setWidth(140, Unit.PIXELS);
		followUpStatusFilter.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, ContactIndexDto.FOLLOW_UP_STATUS));
		followUpStatusFilter.addItems((Object[])FollowUpStatus.values());
		followUpStatusFilter.addValueChangeListener(e -> {
			grid.setFollowUpStatusFilter(((FollowUpStatus)e.getProperty().getValue()));
		});
		filterLayout.addComponent(followUpStatusFilter);

		ComboBox reportedByFilter = new ComboBox();
		reportedByFilter.setWidth(140, Unit.PIXELS);
		reportedByFilter.setInputPrompt("Reported By");
		reportedByFilter.addItems((Object[]) UserRole.values());
		reportedByFilter.addValueChangeListener(e -> {
			grid.setReportedByFilter((UserRole) e.getProperty().getValue());
		});
		filterLayout.addComponent(reportedByFilter);

		TextField searchField = new TextField();
		searchField.setWidth(200, Unit.PIXELS);
		searchField.setInputPrompt(I18nProperties.getPrefixFieldCaption(ContactIndexDto.I18N_PREFIX, SEARCH_FIELD));
		searchField.addTextChangeListener(e -> {
			grid.filterByText(e.getText());
		});
		filterLayout.addComponent(searchField);

		return filterLayout;
	}

	public HorizontalLayout createStatusFilterBar() {
		HorizontalLayout statusFilterLayout = new HorizontalLayout();
		statusFilterLayout.setSpacing(true);
		statusFilterLayout.setSizeUndefined();
		statusFilterLayout.addStyleName(CssStyles.VSPACE_3);

		statusButtons = new HashMap<>();

		Button statusAll = new Button("All", e -> processStatusChange(null, e.getButton()));
		CssStyles.style(statusAll, ValoTheme.BUTTON_LINK, CssStyles.LINK_HIGHLIGHTED);
		statusAll.setCaptionAsHtml(true);
		statusFilterLayout.addComponent(statusAll);
		statusButtons.put(statusAll, "All");

		for (ContactStatus status : ContactStatus.values()) {
			Button statusButton = new Button(status.toString(), e -> processStatusChange(status, e.getButton()));
			CssStyles.style(statusButton, ValoTheme.BUTTON_LINK, CssStyles.LINK_HIGHLIGHTED, CssStyles.LINK_HIGHLIGHTED_LIGHT);
			statusButton.setCaptionAsHtml(true);
			statusFilterLayout.addComponent(statusButton);
			statusButtons.put(statusButton, status.toString());
		}

		// Bulk operation dropdown
		if (LoginHelper.hasUserRight(UserRight.PERFORM_BULK_OPERATIONS)) {
			statusFilterLayout.setWidth(100, Unit.PERCENTAGE);

			MenuBar bulkOperationsDropdown = new MenuBar();	
			MenuItem bulkOperationsItem = bulkOperationsDropdown.addItem("Bulk Actions", null);

			Command changeCommand = selectedItem -> {
				ControllerProvider.getContactController().showBulkContactDataEditComponent(grid.getSelectedRows(), null);
			};
			bulkOperationsItem.addItem("Edit...", FontAwesome.ELLIPSIS_H, changeCommand);
			
			Command cancelFollowUpCommand = selectedItem -> {
				ControllerProvider.getContactController().cancelFollowUpOfAllSelectedItems(grid.getSelectedRows(), new Runnable() {
					public void run() {
						grid.deselectAll();
						grid.reload();
					}
				});
			};
			bulkOperationsItem.addItem("Cancel follow-up", FontAwesome.TIMES, cancelFollowUpCommand);

			Command lostToFollowUpCommand = selectedItem -> {
				ControllerProvider.getContactController().setAllSelectedItemsToLostToFollowUp(grid.getSelectedRows(), new Runnable() {
					public void run() {
						grid.deselectAll();
						grid.reload();
					}
				});
			};
			bulkOperationsItem.addItem("Set to lost to follow-up", FontAwesome.UNLINK, lostToFollowUpCommand);
			
			Command deleteCommand = selectedItem -> {
				ControllerProvider.getContactController().deleteAllSelectedItems(grid.getSelectedRows(), new Runnable() {
					public void run() {
						grid.deselectAll();
						grid.reload();
					}
				});
			};
			bulkOperationsItem.addItem("Delete", FontAwesome.TRASH, deleteCommand);

			statusFilterLayout.addComponent(bulkOperationsDropdown);
			statusFilterLayout.setComponentAlignment(bulkOperationsDropdown, Alignment.TOP_RIGHT);
			statusFilterLayout.setExpandRatio(bulkOperationsDropdown, 1);
		}

		activeStatusButton = statusAll;
		return statusFilterLayout;
	}

	private void updateActiveStatusButtonCaption() {
		if (activeStatusButton != null) {
			activeStatusButton.setCaption(statusButtons.get(activeStatusButton) + LayoutUtil.spanCss(CssStyles.BADGE, String.valueOf(grid.getContainer().size())));
		}
	}

	private void processStatusChange(ContactStatus contactStatus, Button button) {
		grid.setStatusFilter(contactStatus);
		statusButtons.keySet().forEach(b -> {
			CssStyles.style(b, CssStyles.LINK_HIGHLIGHTED_LIGHT);
			b.setCaption(statusButtons.get(b));
		});
		CssStyles.removeStyles(button, CssStyles.LINK_HIGHLIGHTED_LIGHT);
		activeStatusButton = button;
		updateActiveStatusButtonCaption();
	}
	
	@Override
	public void enter(ViewChangeEvent event) {
		grid.reload();
		updateActiveStatusButtonCaption();
	}
}
