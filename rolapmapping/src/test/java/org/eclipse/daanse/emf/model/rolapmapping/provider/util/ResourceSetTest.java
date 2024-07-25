/*
* Copyright (c) 2024 Contributors to the Eclipse Foundation.
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*   SmartCity Jena - initial
*   Stefan Bischof (bipolis.org) - initial
*/
package org.eclipse.daanse.emf.model.rolapmapping.provider.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;

import org.eclipse.daanse.emf.model.rolapmapping.Catalog;
import org.eclipse.daanse.emf.model.rolapmapping.RolapMappingPackage;
import org.eclipse.daanse.emf.model.rolapmapping.util.EmfModelTransformer;
import org.eclipse.daanse.rolap.mapping.api.model.CatalogMapping;
import org.eclipse.daanse.rolap.mapping.mondrian.api.RolapMappingTransformer;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@RequireEMF
public class ResourceSetTest {

    private static String BASE_DIR = System.getProperty("basePath");

    @TempDir
    Path tempDir;

    @Test
    public void write(@InjectService RolapMappingTransformer transformer,
            @InjectService(cardinality = 1, filter = "(" + EMFNamespaces.EMF_MODEL_NAME + "="
                    + RolapMappingPackage.eNAME + ")") ServiceAware<ResourceSet> saResourceSet)
            throws SQLException, InterruptedException, IOException {
        assertThat(saResourceSet.getServices()).hasSize(1);

        String data = Files.readString(Path.of(BASE_DIR, "src/test/resources/schema.xml"));
        CatalogMapping catalogMapping = transformer.transform(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
        assertThat(catalogMapping).isNotNull();

        EmfModelTransformer emfMoTra = new EmfModelTransformer();

        Catalog emfCatalog = emfMoTra.transform(catalogMapping);
        ResourceSet rs = saResourceSet.getService();

        Path file = Files.createTempFile(tempDir, "out", ".xmi");
        URI uri = URI.createFileURI(file.toAbsolutePath().toString());
        Resource resource = rs.createResource(uri);
        resource.getContents().add(emfCatalog);

        resource.save(Map.of());
        System.out.println(Files.readString(file));

    }

}
