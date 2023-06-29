package org.cyclonedx.util.deserializer;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cyclonedx.model.Component;
import org.cyclonedx.model.LicenseChoice;
import org.cyclonedx.model.Metadata;
import org.cyclonedx.model.OrganizationalContact;
import org.cyclonedx.model.OrganizationalEntity;
import org.cyclonedx.model.Property;
import org.cyclonedx.model.Service;
import org.cyclonedx.model.Tool;
import org.cyclonedx.model.metadata.ToolChoice;

public class MetadataDeserializer
    extends JsonDeserializer<Metadata> {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Metadata deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);

    Metadata metadata = new Metadata();

    // Parsing other fields in the Metadata object
    if(node.has("authors")) {
      JsonNode authorsNode = node.get("authors");
      List<OrganizationalContact> authors = new ArrayList<>();

      if (authorsNode.isArray()) {
        for (JsonNode authorNode : authorsNode) {
          OrganizationalContact author = mapper.convertValue(authorNode, OrganizationalContact.class);
          authors.add(author);
        }
      } else if (authorsNode.isObject()) {
        OrganizationalContact author = mapper.convertValue(authorsNode, OrganizationalContact.class);
        authors.add(author);
      }

      metadata.setAuthors(authors);
    }

    if(node.has("component")) {
      Component component = mapper.convertValue(node.get("component"), Component.class);
      metadata.setComponent(component);
    }

    if (node.has("manufacture")) {
      OrganizationalEntity manufacture = mapper.convertValue(node.get("manufacture"), OrganizationalEntity.class);
      metadata.setManufacture(manufacture);
    }

    if (node.has("supplier")) {
      OrganizationalEntity supplier = mapper.convertValue(node.get("supplier"), OrganizationalEntity.class);
      metadata.setSupplier(supplier);
    }

    if(node.has("license")) {
      LicenseChoice license = mapper.convertValue(node.get("license"), LicenseChoice.class);
      metadata.setLicenseChoice(license);
    }

    /*JsonDeserializer<Date> customDateDeserializer = new CustomDateDeserializer();
    Date timestamp = customDateDeserializer.deserialize(p, ctxt);
    metadata.setTimestamp(timestamp);*/

    if(node.has("properties")) {
      List<Property> properties = mapper.convertValue(node.get("properties"), new TypeReference<List<Property>>() { });
      metadata.setProperties(properties);
    }

    JsonNode toolsNode = node.get("tools");

    if (toolsNode != null) {
      // Check if the 'tools' field is an array or an object
      if (toolsNode.isArray()) {
        // If it's an array, treat it as a list of tools for the old version
        List<Tool> tools = mapper.convertValue(toolsNode, new TypeReference<List<Tool>>() { });
        metadata.setTools(tools);
      }
      else {
        // If it's an object, treat it as a ToolChoice for the new version
        ToolChoice toolChoice = new ToolChoice();
        if (toolsNode.has("components")) {
          List<Component> components =
              mapper.convertValue(toolsNode.get("components"), new TypeReference<List<Component>>() { });
          toolChoice.setComponents(components);
        }
        if (toolsNode.has("services")) {
          List<Service> services =
              mapper.convertValue(toolsNode.get("services"), new TypeReference<List<Service>>() { });
          toolChoice.setServices(services);
        }
        metadata.setToolChoice(toolChoice);
      }
    }

    return metadata;
  }
}
