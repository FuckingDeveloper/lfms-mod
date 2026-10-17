# Managed resources and standard Java services

Runtime120 passed the old public texture service read and source-owned atlas
construction, then standard ServiceLoader.load(Class) found no JEI platform
provider. ManagedLegacyClassLoader currently exposes classes but no JAR resources;
source lifecycle callbacks also inherit the target thread context loader.

Extend the established resolved-dependency visibility contract to ordinary
resources and META-INF/services. Preserve original descriptors and use the genuine
JDK ServiceLoader with the managed owner's context loader during source scopes.
No provider-name selectors, arbitrary sibling visibility, replacement service
registry or silent empty provider results are permitted. Source/profile type-name
projection inside descriptor contents is outside this initial family.
