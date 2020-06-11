/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.api.utils.fieldvisibility.checkers;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.utils.Diseases;
import de.symeda.sormas.api.utils.fieldvisibility.FieldVisibilityCheckers;

public class DiseaseFieldVisibilityChecker implements FieldVisibilityCheckers.FieldNameBaseChecker {

	private final Disease disease;

	public DiseaseFieldVisibilityChecker(Disease disease) {
		this.disease = disease;
	}

	@Override
	public boolean isVisible(Class<?> parentType, String propertyId) {
		return Diseases.DiseasesConfiguration.isDefinedOrMissing(parentType, propertyId, disease);
	}
}
