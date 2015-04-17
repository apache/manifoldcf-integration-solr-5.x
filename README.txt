# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


Compatibility
------------

This version of this component is fully functional with Apache ManifoldCF 1.6 and
above.  It is backwards compatible with earlier versions as well, except for the fact
that two additional Solr fields are required for this plugin to work.


Instructions for Building Apache ManifoldCF Plugin for Apache Solr 5.x from Source
------------------------------------------------------------------------------

1. Download the Java SE 7 JDK (Java Development Kit), or greater, from
   http://www.oracle.com/technetwork/java/index.html.
   You will need the JDK installed, and the %JAVA_HOME%\bin directory included
   on your command path.  To test this, issue a "java -version" command from your
   shell and verify that the Java version is 1.7 or greater.

2. Download and install Maven 3.0 or later.  Maven installation and configuration
   instructions can be found here:  http://maven.apache.org

3. Building distribution assemblies

   Execute the following command in order to build the distribution assemblies

   mvn package assembly:assembly

   The JAR package can be found in the target folder:

   target/apache-manifoldcf-solr-5.x-plugin-<VERSION>.jar where <VERSION> is the release version


Getting Started
---------------

There are two ways to hook up security to Solr in this package.  The first is using
a Query Parser plugin.  The second is using a Search Component.  In both cases,
the first step is to have ManifoldCF installed and running.  See:
http://manifoldcf.apache.org/release/trunk/en_US/how-to-build-and-deploy.html

Then, in order to store the document authorization information you need to add
'allow' and 'deny' fields for documents, parents, and shares to your Solr
index. Depending on your schemaFactory setting in solrconfig.xml you have to 
use either the schema.xml file for 'ClassicIndexSchemaFactory' or the schema
API for 'ManagedIndexSchemaFactory'. 
See section 'Managed Schema Definition in SolrConfig' in the Solr Reference Gude
https://www.apache.org/dyn/closer.cgi/lucene/solr/ref-guide/apache-solr-ref-guide-5.0.pdf

For schema.xml simply add the following field definitions to the <schema>
section:

  <field name="allow_token_document" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>
  <field name="allow_token_parent" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>
  <field name="allow_token_share" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>
  <field name="deny_token_document" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>
  <field name="deny_token_parent" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>
  <field name="deny_token_share" type="string" indexed="true" stored="true"
    multiValued="true" required="true" default="__nosecurity__"/>

To define the fields via schema API use the curl command instead:

curl -X POST -H 'Content-type:application/json' --data-binary '{ 
  "add-field" : [
  { "name":"allow_token_document", "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true",
    "default":"__nosecurity__" },
  { "name":"allow_token_parent",   "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true",
    "default":"__nosecurity__" },
  { "name":"allow_token_share",    "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true", 
    "default":"__nosecurity__" },
  { "name":"deny_token_document",  "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true",
    "default":"__nosecurity__" },
  { "name":"deny_token_parent",    "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true",
    "default":"__nosecurity__" },
  { "name":"deny_token_share",     "type":"string", "indexed":"true",
    "stored":"true", "multiValued":"true", "required":"true",
    "default":"__nosecurity__"}
]}' http://localhost:8983/solr/<collection_name>/schema

Replace <collection_name> with your core or collection name respectively.

The default value of "__nosecurity__" is manadatory because the queries will be
rewritten to use all of these 6 fields. If a field is missing in the index then
you will get no results for your search.

Check the field definitions with

curl http://localhost:8983/solr/<collection_name>/schema/fields


Using the Query Parser Plugin
-----------------------------

To set up the query parser plugin, modify your solrconfig.xml to add the query parser:

  <!-- ManifoldCF document security enforcement component -->
  <queryParser name="manifoldCFSecurity"
    class="org.apache.solr.mcf.ManifoldCFQParserPlugin">
    <str name="AuthorityServiceBaseURL">http://localhost:8345/mcf-authority-service</str>
    <int name="ConnectionPoolSize">50</int>
  </queryParser>

Hook up the search component in the solrconfig.xml file wherever you want it, e.g.:

<requestHandler name="/select" class="solr.SearchHandler">
  <lst name="appends">
    <str name="fq">{!manifoldCFSecurity}</str>
  </lst>
  ...
</requestHandler>


Using the Search Component
--------------------------

To set up the search component, modify your solrconfig.xml to add the search component:

  <!-- ManifoldCF document security enforcement component -->
  <searchComponent name="manifoldCFSecurity"
    class="org.apache.solr.mcf.ManifoldCFSearchComponent">
    <str name="AuthorityServiceBaseURL">http://localhost:8345/mcf-authority-service</str>
    <int name="ConnectionPoolSize">50</int>
  </searchComponent>

Hook up the search component in the solrconfig.xml file wherever you want it, e.g.:

<requestHandler name="/select" class="solr.SearchHandler">
  <arr name="last-components">
    <str>manifoldCFSecurity</str>
  </arr>
  ...
</requestHandler>


Supplying authenticated usernames and domains
----------------------------------------------

This component looks for the following parameters in the Solr request object:

AuthenticatedUserName
AuthenticatedUserDomain
AuthenticatedUserName_XX
AuthenticatedUserDomain_XX

At a minimum, AuthenticatedUserName must be present in order for the component to communicate with
the ManifoldCF Authority Service and obtain user access tokens.  In that case, the user identity will consist
of one user and one authorization domain.  If AuthenticatedUserDomain is not set, then the authorization domain
chosen will be the standard default domain, an empty string.

If you need multiple user/domain tuples for the user identity, you may pass these as parameter pairs starting with
AuthenticatedUserName_0 and AuthenticatedUserDomain_0, and counting up as high as you like.

Operation in conjunction with mod-authz-annotate
------------------------------------------------

An optional component of ManifoldCF can be built and deployed as part of Apache - mod-authz-annotate.  The
mod-authz-annotate module obtains the Kerberos principal from the Kerberos tickets present if mod-auth-kerb is used, and uses
the MCF Authority Service to look up the appropriate access tokens.  If you choose to use that architecture,
you will still need to use this Solr component to modify the user query.  All you have to do is the following:

- Make sure you do not set AuthenticatedUserName or AuthenticatedUserName_0 in the request
- Make sure the HTTP request from Apache to Solr translates all AAAGRP header values into "UserToken" parameters
   for the Solr request



Licensing
---------

Apache ManifoldCF Plugin for Apache Solr 5.x is licensed under the
Apache License 2.0. See the files called LICENSE.txt and NOTICE.txt
for more information.

Cryptographic Software Notice
-----------------------------

This distribution may include software that has been designed for use
with cryptographic software. The country in which you currently reside
may have restrictions on the import, possession, use, and/or re-export
to another country, of encryption software. BEFORE using any encryption
software, please check your country's laws, regulations and policies
concerning the import, possession, or use, and re-export of encryption
software, to see if this is permitted. See <http://www.wassenaar.org/>
for more information.

The U.S. Government Department of Commerce, Bureau of Industry and
Security (BIS), has classified this software as Export Commodity
Control Number (ECCN) 5D002.C.1, which includes information security
software using or performing cryptographic functions with asymmetric
algorithms. The form and manner of this Apache Software Foundation
distribution makes it eligible for export under the License Exception
ENC Technology Software Unrestricted (TSU) exception (see the BIS
Export Administration Regulations, Section 740.13) for both object
code and source code.

The following provides more details on the included software that
may be subject to export controls on cryptographic software:

  The Apache ManifoldCF Solr 5.x Plugin does not include any
  implementation or usage of cryptographic software at this time.
  
Contact
-------

  o For general information visit the main project site at
    http://manifoldcf.apache.org

