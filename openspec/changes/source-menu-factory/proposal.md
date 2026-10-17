# Menu factory and opening-data family

Deferred registry suppliers may construct menus using source IForgeMenuType and
IContainerFactory after entrypoint admission. Source and native factories differ
in SAM buffer type, so a namespace rename does not preserve the binary contract.

Provide a source-shaped interface, native buffer forwarding and actual native
MenuType interface integration. Preserve custom source implementations, default
null-data entrypoints and raw-buffer identity. Use shared method-catalog rules.
No custom network framework or original mod JAR changes are required. Opening
packet delivery and actual screens remain separate functional gates.
