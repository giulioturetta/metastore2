/*
 * Copyright 2023 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.metastore2.health;

import edu.kit.datamanager.metastore2.configuration.MetastoreConfiguration;
import edu.kit.datamanager.metastore2.dao.ISchemaRecordDao;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

/**
 *
 */
public abstract class HealthCheck implements HealthIndicator, InfoContributor {

  private static final Logger LOG = LoggerFactory.getLogger(HealthCheck.class);

  /**
   * Determine all details for given directory.
   * @param pathUrl URL of directory
   * @return Map with details.
   */
  protected final Map<String, String> testDirectory(URL pathUrl) {
    Map<String, String> properties = new HashMap<>();
    String totalSpace = null;
    String freeSpace = null;
    try {
      Path path = Paths.get(pathUrl.toURI());
      Path probe = Paths.get(path.toString(), "probe.txt");
      try {
        probe = Files.createFile(probe);
        Files.write(probe, "Success".getBytes());
        File repoDir = path.toFile();
        double total = (double) repoDir.getTotalSpace();
        double free = (double) repoDir.getFreeSpace();
        totalSpace = String.format("%.2f GB", total / 1073741824);
        freeSpace = String.format("%.2f GB (%.0f%%)", (double) repoDir.getFreeSpace() / 1073741824, free * 100.0 / total);
        properties.put("Total space", totalSpace);
        properties.put("Free space", freeSpace);

      } catch (Throwable t) {
        LOG.error("Failed to check repository folder at '" + path.toString() + "'. Returning negative health status.", t);
      } finally {
        try {
          Files.deleteIfExists(probe);
        } catch (Throwable ignored) {
        }
      }
    } catch (URISyntaxException ex) {
      LOG.error("Invalid base path uri of '" + pathUrl.toString() + "'.", ex);
    }
    return properties;
  }

}
