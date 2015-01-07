/**
 * Copyright (C) 2014 Oliver Schünemann
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 29.12.2014
 * @version 1.0
 * @author oliver
 */
package oc.resolve;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import oc.io.StreamIo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author oliver
 * 
 */
public class Main {

	public static final Set<String> tags = new HashSet<>(Arrays.asList("source", "building",
			"highway", "addr:housenumber", "name", "addr:street", "addr:city", "addr:postcode",
			"source:date", "created_by", "addr:country", "natural", "tiger:cfcc", "tiger:county",
			"tiger:reviewed", "landuse", "waterway", "start_date", "wall", "ref:bag", "surface",
			"power", "attribution", "oneway", "tiger:name_base", "tiger:source", "tiger:tlid",
			"amenity", "tiger:name_type", "ref", "tiger:upload_uuid", "tiger:separated",
			"yh:WIDTH", "tiger:zip_left", "maxspeed", "access", "lanes", "tiger:zip_right", "note",
			"barrier", "yh:STRUCTURE", "yh:TYPE", "source_ref", "service", "yh:TOTYUMONO",
			"yh:WIDTH_RANK", "addr:place", "building:levels", "source:addr", "tracktype", "layer",
			"type", "place", "foot", "bicycle", "addr:conscriptionnumber", "note:ja", "height",
			"railway", "is_in", "osak:identifier", "leisure", "osak:revision", "KSJ2:curve_id",
			"bridge", "osak:municipality_no", "ref:ruian:addr", "ele", "KSJ2:long", "KSJ2:lat",
			"KSJ2:coordinate", "KSJ2:filename", "NHD:FCode", "NHD:ComID", "operator",
			"NHD:ReachCode", "3dshapes:ggmodelk", "NHD:RESOLUTION", "addr:interpolation",
			"osak:street_no", "shop", "nhd:com_id", "man_made", "admin_level", "nhd:reach_code",
			"is_in:state", "NHD:FType", "NHD:way_id", "is_in:state_code", "boundary",
			"gnis:feature_id", "lit", "name:en", "addr:state", "tiger:name_direction_prefix",
			"fixme", "nhd:fdate", "nycdoitt:bin", "gnis:fcode", "is_in:country", "import",
			"source:name", "tiger:name_base_1", "gnis:ftype", "width", "tourism", "gauge",
			"kms:municipality_no", "gnis:created", "is_in:country_code", "kms:county_name",
			"kms:county_no", "kms:street_no", "kms:municipality_name", "kms:street_name",
			"kms:last_updated", "kms:zip_name", "kms:zip_no", "kms:house_no", "name_1",
			"electrified", "sport", "gnis:county_id", "gnis:state_id", "kms:parish_name",
			"kms:parish_no", "intermittent", "chicago:building_id", "public_transport", "tunnel",
			"area", "is_in:city", "NHD:FDate", "addr:suburb", "NHD:FTYPE", "osak:subdivision",
			"addr:streetnumber", "wheelchair", "entrance", "voltage", "crossing", "motor_vehicle",
			"religion", "description", "website", "addr:street:name", "it:fvg:ctrn:code", "wood",
			"it:fvg:ctrn:revision", "ref:ruian", "addr:street:type", "parking", "source:file",
			"geobase:acquisitionTechnique", "building:use", "ref:ruian:building", "note:es",
			"ngbe:id", "ngbe:grupo", "ngbe:subgrupo", "ngbe:hojabcn25", "ngbe:tema", "ngbe:codigo",
			"ngbe:huso", "ngbe:xutm_ed50", "ngbe:lon_ed50", "ngbe:yutm_ed50", "ngbe:lat_ed50",
			"ngbe:version", "ngbe:tipotexto", "network", "AND_nosr_r", "roof:shape", "alt_name",
			"frequency", "horse", "name:ru", "addr:street:prefix", "cycleway", "water",
			"wikipedia", "LINZ:source_version", "source:maxspeed", "sidewalk", "denotation",
			"postal_code", "note:en", "geobase:uuid", "geobase:datasetName", "species",
			"tiger:name_direction_suffix", "LINZ:layer", "addr:district", "phone", "bus",
			"addr:full", "historic", "addr:provisionalnumber", "LINZ:dataset", "route",
			"emergency", "hgv", "junction", "population", "building:material", "yh:LINE_NAME",
			"yh:LINE_NUM", "noexit", "gnis:id", "shelter", "restriction", "tiger:name_type_1",
			"denomination", "addr:region", "opening_hours", "int_name", "source:building",
			"KSJ2:LIN", "usage", "cuisine", "CLC:code", "motorcar", "segregated", "nhd:fcode",
			"nhd:ftype", "name:ja", "CLC:year", "building:flats", "KSJ2:RAC_label", "KSJ2:OPC",
			"KSJ2:RAC", "KSJ2:INT_label", "KSJ2:INT", "addr:inclusion", "ref:UrbIS",
			"building:ruian:type", "osak:street", "KSJ2:RIC", "KSJ2:WSC", "KSJ2:DFD",
			"KSJ2:COP_label", "name:de", "name:fr", "sac_scale", "url", "mtb:scale", "footway",
			"aeroway", "naptan:verified", "traffic_sign", "gst:feat_id", "KSJ2:LOC",
			"building:usage:pl", "KSJ2:curve_type", "boat", "stream", "smoothness", "int_ref",
			"addr:housename", "information", "import_uuid", "fire_hydrant:type", "source:geometry",
			"mvdgis:cod_nombre", "usar_addr:edit_date", "maaamet:ETAK", "KSJ2:river_id",
			"KSJ2:RIN", "naptan:AtcoCode", "tiger:zip_left_1", "dibavod:id", "naptan:CommonName",
			"name:ar", "source:ja", "naptan:Bearing", "mvdgis:padron", "KSJ2:ARE", "wetland",
			"naptan:Street", "material", "project:eurosha_2012", "leaf_type", "massgis:way_id",
			"AND:importance_level", "motorcycle", "route_ref", "fee", "genus", "CLC:id",
			"capacity", "roof:colour", "name:fi", "circumference", "cladr:code", "NHD:Permanent_",
			"naptan:Indicator", "seamark:type", "designation", "FIXME", "KSJ2:LPN", "KSJ2:lake_id",
			"kms:city_name", "generator:source", "trail_visibility", "canvec:UUID", "old_ref",
			"NHD:Resolution", "living_street", "tracks", "bench", "is_in:region",
			"naptan:NaptanCode", "backrest", "gnis:County", "gnis:Class", "gnis:ST_num",
			"gnis:County_num", "traffic_calming", "cables", "gnis:ST_alpha", "source:position",
			"name:ar1", "WroclawGIS:building:date", "WroclawGIS:building:ID",
			"WroclawGIS:building:layer", "condition", "uir_adr:ADRESA_KOD", "canvec:CODE",
			"roof:material", "building:fireproof", "building:use:pl", "NHS", "name:uk",
			"name:ko_rm", "accuracy:meters", "ncat", "incline", "fire_hydrant:diameter",
			"diameter_crown", "naptan:Landmark", "KSJ2:ADS", "KSJ2:PubFacAdmin", "is_in:province",
			"converted_by", "design", "crossing_ref", "covered", "golf", "ref:FR:FANTOIR",
			"dcgis:gis_id", "gnis:county_name", "is_in:county", "taxon", "addr:county",
			"species:de", "building:roof", "tiger:name_base_2", "site", "building:part",
			"building:colour", "toll", "colour", "construction", "KSJ2:AdminArea", "tree:ref",
			"statscan:rbuid", "piste:type", "survey:date", "addr:city:it", "addr:city:de", "brand",
			"comment", "building:type", "is_in:municipality", "source:ref", "ref:bagid",
			"bag:extract", "hiking", "old_name", "fire_hydrant:position", "SK53_bulk:load",
			"naptan:PlusbusZoneRef", "name:be", "adr_les", "name:zh", "CLC:shapeId",
			"bak:fac_type1", "bag:status", "cladr:suffix", "CLC:explanation", "residential", "atm",
			"cladr:name", "zone", "OBJTYPE", "bak:fac_type2", "OPPDATERIN", "source:ro", "level",
			"tiger:name_direction_prefix_1", "tiger:buildingType", "HFCS", "tower:type",
			"dcgis:captureyear", "addr:street:de", "addr:street:it", "is_in:continent", "standing",
			"vehicle", "bag:gebruiksdoel", "office", "odbl", "dataset", "teryt:simc", "motorroad",
			"bmo:type", "old_railway_operator", "addr:province", "fence_type", "roof:orientation",
			"lcn", "hgv:national_network", "GeoBaseNHN:VALDATE", "genus:it", "tiger:zip_right_1",
			"species:it", "seats", "destination", "primary_use", "length", "ref:INSEE",
			"surrey:addrid", "surrey:date", "roof:levels", "source:hgv:national_network",
			"embankment", "rer_edi_id:ref", "email", "uuid:building", "contact:phone",
			"turn:lanes", "REF", "FLATE", "roof:height", "trees", "tactile_paving",
			"population:date", "geobase:UUID", "maxspeed:note", "meadow", "crop", "import_ref",
			"ewmapa:warstwa", "local_ref", "loc_ref", "maxweight", "origen", "location",
			"source:tracer", "other_use", "secondary_use", "border_type", "teryt:sym_ul",
			"gnis:feature_type", "building:structure", "building:type:pl", "nat_ref",
			"dcgis:square", "source:outline", "length_unit", "name:ko", "name_2", "official_name",
			"distance", "psv", "dcgis:lot", "lbcs:activity:code", "lbcs:function:code",
			"lbcs:activity:name", "aerialway", "GNS:id", "lbcs:function:name", "clc:code",
			"gnis:import_uuid", "name:ja_rm", "review", "source:url", "building:roof:shape",
			"GNS:dsg_code", "name:sv", "name:genitive", "GNS:dsg_string", "owner",
			"official_status", "to", "piste:difficulty", "leaf_cycle", "from", "gnis:reviewed",
			"uic_ref", "building:place", "source:zoomlevel", "name:botanical", "addr:hamlet",
			"WroclawGIS:addr:date", "WroclawGIS:addr:layer", "WroclawGIS:addr:id",
			"WroclawGIS:addr:postcode:id", "WroclawGIS:addr:postcode:layer", "source:population",
			"name:he", "tiger:name_direction_suffix_1", "ref:FR:fantoir", "b5m:urlOrto", "b5m:id",
			"b5m:url", "name:prefix", "lanes:forward", "recycling:glass", "date", "zone:traffic",
			"seamark:name", "ford", "Type", "abutters", "is_in:village", "source:postcode",
			"typhoon:damage", "nhd-shp:fdate", "nhd-shp:com_id", "tiger:zip_left_2", "ID",
			"ref:opendataparis:adresse", "ref:opendataparis:geo_point_2d",
			"ref:opendataparis:domanialit", "taxon:species", "generator:output:electricity",
			"divipola", "KSJ2:ODC", "lanes:backward", "naptan:ShortCommonName", "fax",
			"contact:website", "building:walls", "maxspeed:type", "naptan:StopAreaCode",
			"naptan:StopAreaType", "source_date", "vending", "addr:subdistrict", "direction",
			"snowmobile", "naptan:Crossing", "NHD:FLOWDIR", "wires", "NHD:Elevation", "cosmha",
			"nhd-shp:fcode", "disused", "nhd:gnis_id", "GeoBaseNHN:DatasetName", "osmc:symbol",
			"supervised", "generator:method", "uic_name", "source_type_imagery",
			"railway:traffic_mode", "dispensing", "bicycle_parking", "bag:bouwjaar",
			"shelter_type", "park_ride", "waterway:llid", "massgis:SOURCE", "massgis:IT_VALDESC",
			"moped", "recycling_type", "gtfs_id", "history", "maxheight", "ownership", "clc:id",
			"accuracy", "heritage", "USGS-LULC:CLASS", "USGS-LULC:LEVEL_I", "USGS-LULC:LEVEL_II",
			"USGS-LULC:STATECTY", "USGS-LULC:CNTYNAME", "massgis:PALIS_ID", "massgis:OBJECTID",
			"massgis:WETCODE", "massgis:SOURCE_SCA", "massgis:POLY_CODE", "massgis:IT_VALC",
			"mml:ref", "est_width", "house", "project", "piste:grooming",
			"seamark:light:character", "NHD:GNIS_ID", "capacity:disabled", "source:en", "smoking",
			"LandPro08:LC_NAME", "LandPro08:DE3", "LandPro08:LC", "LandPro08:DE5",
			"LandPro08:LU_NAME", "LandPro08:LCLU", "LandPro08:LU", "building:height",
			"operator:type", "Source", "internet_access", "name:sr", "power_source", "image",
			"id_origin", "zone:maxspeed", "seamark:light:colour",
			"TMC:cid_58:tabcd_1:LocationCode", "TMC:cid_58:tabcd_1:Class", "KSJ2:PRC",
			"KSJ2:DFC_label", "KSJ2:PRC_label", "KSJ2:DFC", "KSJ2:BDC", "KSJ2:forest_id",
			"TMC:cid_58:tabcd_1:LCLversion", "mtb:scale:uphill", "cesena:ref_id", "mtb", "hight",
			"wikipedia:de", "symbol", "species:nl", "heritage:operator", "cutting", "unsigned_ref",
			"fuel:diesel", "gns:dsg", "snowplowing", "content", "tiger:name_type_2", "koatuu",
			"BMO:TYPE", "status", "todo", "board_type", "ski", "parking:lane:both", "railway:pzb",
			"name:sr-Latn", "indoor", "collection_times", "is_in:province_code", "loc_name",
			"wikipedia:ru", "gtfs:id", "recycling:paper", "seamark:buoy_lateral:colour",
			"proposed", "operator:ja", "seamark:buoy_lateral:category", "step_count",
			"seamark:buoy_lateral:shape", "massgis:FEE_OWNER", "massgis:PUB_ACCESS",
			"massgis:TOWN_ID", "massgis:PRIM_PURP", "massgis:FEESYM", "massgis:OWNER_TYPE",
			"massgis:DCAM_ID", "massgis:FY_FUNDING", "massgis:DEED_ACRES", "massgis:EOEAINVOLV",
			"massgis:ARTICLE97", "massgis:ATT_DATE", "massgis:LEV_PROT", "massgis:OS_DEED_BO",
			"massgis:OS_DEED_PA", "massgis:POLY_ID", "massgis:OS_ID", "massgis:ASSESS_ACR",
			"trolley_wire", "protected", "maxspeed:conditional", "rcn_ref", "openGeoDB:loc_id",
			"building:foundation_height", "destination:ref", "openGeoDB:is_in", "tram", "goods",
			"clc:year", "openGeoDB:community_identification_number", "openGeoDB:layer",
			"openGeoDB:is_in_loc_id", "sby:bldgtype", "zip_left", "agricultural", "bag:begindatum",
			"naptan:BusStopType", "addr:hamlet:de", "addr:hamlet:it", "openGeoDB:auto_update",
			"zip_right", "short_name", "social_facility", "exit_to", "maintenance",
			"openGeoDB:postal_codes", "name:it", "fuel:octane_95", "contact:email", "tmc",
			"openGeoDB:version", "genus:de", "openGeoDB:name", "openGeoDB:sort_name", "addr:flats",
			"seamark:light:period", "tiger:mtfcc", "KSJ2:BDC_label", "source:ele", "mml:class",
			"FMMP_modified", "tiger:STATEFP", "openGeoDB:type", "FMMP_reviewed", "lamp_type",
			"recycling:clothes", "turn:lanes:forward", "gnis:edited", "train", "railway:radio",
			"military", "highspeed", "tiger:MTFCC", "teryt:terc", "FID", "acres",
			"construction_date", "payment:coins", "DeKalb:id", "massgis:SITE_NAME", "gvr:code",
			"rednap:latitud", "rednap:longitud", "source:wfs", "rednap:posicion", "rednap:tipo",
			"rednap:ficha", "rednap:codigoine", "rednap:numero",
			"TMC:cid_58:tabcd_1:NextLocationCode", "rednap:grupo", "rednap:nodo",
			"TMC:cid_58:tabcd_1:PrevLocationCode", "name:es", "tiger:CLASSFP", "tiger:LSAD",
			"tiger:FUNCSTAT", "tiger:NAMELSAD", "tiger:NAME", "tiger:PLACEFP", "tiger:PLACENS",
			"tiger:PLCIDFP", "tiger:CPI", "tiger:PCICBSA", "tiger:PCINECTA", "circumfere",
			"ref:FR:bordeaux:tree", "maaamet:orig_tunnus", "note:de", "naptan:Notes", "ref_name",
			"is_in:iso_3166_2", "cycleway:right", "ruins", "openGeoDB:population", "ref:old",
			"ref:ine", "operational_status", "is_in:left", "is_in:right", "massgis:OWNER_ABRV",
			"paloalto_ca:id", "school:FR", "RLIS:localid", "name:nl", "levels", "name:el",
			"openGeoDB:location", "place_name", "waterway:type", "surveillance",
			"source:addr:postcode", "name:en1", "seamark:light:height", "old_ref_legislative",
			"it:lo:FEAT", "it:lo:ID_Z", "it:lo:ID_E", "it:lo:TEMA", "it:lo:STRA", "it:lo:CLAS",
			"it:lo:UN_V", "rednap:altitudortometrica", "TMC:cid_58:tabcd_1:Direction",
			"name:ja_kana", "llid", "ref:field:sector", "name:right", "dcgis:featurecode",
			"name:left", "geonames_id", "us.fo:kommununr", "KSJ2:segment", "highways_agency:area",
			"line", "stop_id", "us.fo:Galdandi_frá", "us.fo:Veganr", "us.fo:Adressutal",
			"us.fo:Postnr", "lamp_mount", "name:zh_pinyin", "craft", "placement", "kvl_hro:type",
			"seamark:mooring:category", "geobase:roadclass", "teryt:rm", "min_height", "branch",
			"building:cladding", "catastro:ref", "bin", "maxspeed:source", "3dshapes:note",
			"teryt:stan_na", "old_ref:1945", "wikidata", "name:ka", "DATAFANGST", "KVALITET",
			"stars", "hgv:state_network", "source:hgv:state_network", "reg_name", "workrules",
			"IBGE:CD_ADMINIS", "geobase:nid", "tiger:name_base_3", "massgis:SOURCE_MAP",
			"parking:lane:right", "seamark:buoy_lateral:system", "species:fr", "traffic_sign:2",
			"garmin_type", "census:population", "reg_ref", "turn:lanes:backward", "taxon:cultivar",
			"oneway:bicycle", "typhoon:reviewed", "manhole", "seamark:light:range",
			"massgis:ASSESS_MAP", "okato:user", "NYSTL:PRINTKEY", "generator:type", "contact:fax",
			"ref:UAI", "note:old_railway_operator", "structure", "name:th", "ref:type", "ntd_id",
			"name:ga", "abandoned", "parking:lane:left", "siruta:code", "massgis:ASSESS_LOT",
			"seamark:light:reference", "ref:FR:SIREN", "wheelchair:description", "resource",
			"teryt:updated_by", "bak:fac_type3", "tiger:zip_right_2", "rcn", "source:highway",
			"noname", "redwood_city_ca:bld_gid", "building_type", "NJDOT_SRI", "gfoss_id",
			"fuel:octane_98", "road_marking", "sorting_name", "dcgis:dataset", "source:lit",
			"priority", "recycling:cans", "wikipedia:en", "ref:FR:INSEE", "mooring", "maxaxleload",
			"chile:region", "ewmapa:kod_znakowy", "lamp_flames", "detail", "maxspeed:forward",
			"mechanical", "technology", "redwood_city_ca:addr_id", "building:min_level",
			"source:hgv", "design:ref", "circuits", "tiger:zip_left_3", "maxspeed:backward",
			"ref:corine_land_cover", "railway:signal:direction", "route_master",
			"building:use:residential", "railway:etcs", "seamark:topmark:shape", "source:imagery",
			"railway:signal:position", "drinking_water", "isced:level",
			"operational_status_quality", "railway:lzb", "bbg:dataset", "bbg:addr", "bbg:stname",
			"seamark:topmark:colour", "taxi", "protection_title", "bbg:stsuffix", "trailblazed",
			"toilets", "building_status", "project:mappingforniger", "source:filename",
			"lamp_operator", "KURVE", "ncn_ref", "kvl_hro:amenity", "addr:place:de",
			"addr:place:it", "omkum:code", "colour:back", "minspeed", "structure_gauge",
			"railway:track_class", "VRS:gemeinde", "VRS:ortsteil", "VRS:ref", "name:kn",
			"ref_catastral", "lamp_model:de", "gns_classification", "canvec:ROADCLASS",
			"source:surface", "canvec:STRUCTYPE", "dgfip_type", "code_commune", "ref:cobe",
			"lamp_ref_swd", "species:en", "metcouncil:site_id", "metcouncil:site_at",
			"metcouncil:site_on", "metcouncil:city_id", "metcouncil:nroutes",
			"metcouncil:corn_desc", "metcouncil:dfz", "metcouncil:site_hfn",
			"metcouncil:corner_loc", "metcouncil:wkdy_trips", "metcouncil:sat_trips",
			"metcouncil:sun_trips", "object:addr:street", "object:addr:postcode",
			"object:addr:city", "pk", "object:addr:country", "ref:IFOPT", "substation",
			"landcover", "gritting", "local_code", "sangis:OBJECTID", "sangis:TYPE",
			"metcouncil:routes", "gtfs_stop_code", "memorial:type", "hazmat", "gns:ufi", "kerb",
			"it:pv:pavia:FEATURE_ID", "it:pv:pavia:ID_ZRIL", "it:pv:pavia:SHAPE_Area",
			"it:pv:pavia:UN_VOL_POR", "it:pv:pavia:STRATO", "it:pv:pavia:CLASSE",
			"it:pv:pavia:SHAPE_Leng", "it:pv:pavia:DELTA_Z", "it:pv:pavia:UN_VOL_AV",
			"it:pv:pavia:EDIFC_TY", "it:pv:pavia:TEMA", "it:pv:pavia:EDIFC_STAT",
			"it:pv:pavia:ID_EDIF", "it:pv:pavia:EDIFC_USO", "pipeline", "ewmapa:funkcja",
			"map_size", "source:maxaxleload", "source:maxaxleload:url", "seamark", "scvwd:ROUTEID",
			"ref:zsj", "gns_uni", "map_type", "practicability", "source_type_survey",
			"colour:text", "artwork_type", "OBJECTID", "ref:sandre", "internet_access:fee",
			"mountain_pass", "AREA", "cxx:code", "cxx:id", "fuel:lpg", "name:ca",
			"old_postal_code", "seamark:fixme", "name:alt", "siruta:county_id", "siruta:name_sup",
			"siruta:county", "siruta:code_sup", "siruta:county_code", "siruta:type",
			"population:census:1992", "IBGE:GEOCODIGO", "surface_survey", "_ID_",
			"railway:bidirectional", "building:soft_storey", "building:overhang",
			"shape:elevation", "building:adjacency", "NHD:GNIS_Name", "postal_code:source",
			"shape:plan", "GeoBaseNHN:PROVIDER", "GeoBaseNHN:UUID", "cycleway:left",
			"railway:position", "nadoloni:id", "PERIMETER", "building:levels:underground",
			"openGeoDB:license_plate_code", "Id", "mappingdc:gid", "seamark:light:ref", "name:lt",
			"de:regionalschluessel", "overtaking", "source:area", "CEMT", "LAYER", "note:name",
			"waterway:sign", "source_type_GPS", "note:lanes", "id_ob",
			"de:amtlicher_gemeindeschluessel", "catmp-RoadID", "maxspeed:practical", "operator:en",
			"area:highway", "median", "source:addr:housenumber", "not:name", "lock", "site_type",
			"massgis:BASE_MAP", "cycling_width", "bridge:name", "end_date", "id", "locality",
			"tiger:countyfp", "tiger:statefp", "species:FR", "fdot:sis", "it:pv:pavia:ANNO_SOGLI",
			"massgis:MANAGER", "massgis:MANAGR_TYP", "seasonal", "maxwidth", "is_in:zh",
			"name:en2", "ine:municipio", "passenger", "gns_ufi", "name:hsb", "addr:municipality",
			"buildingpart", "massgis:MANAGR_ABR", "lines", "ref:en", "name:br",
			"it:fvg:regional_catalog:corsi_acqua:caratteristica",
			"it:fvg:regional_catalog:corsi_acqua:codice_fvg", "name:hu", "globalid",
			"colour:arrow", "memorial:addr", "garden:type", "authoritative", "lot_type",
			"motorboat", "pcode", "driveway", "arpav_codice_bacino", "complete", "hov",
			"tiger:name_direction_prefix_2", "name:pl", "protect_class", "trolleybus",
			"arpav_codice_sottobacino", "is_in:catastro:ref", "rwn_ref", "ref:lukr:DAGS_LEIDR",
			"ref:lukr:VINNSLA_F", "ref:lukr:NAKV", "ref:lukr:UTBSV", "ref:lukr:DAGS_INN",
			"ref:lukr:RUTT", "ref:lukr:VIDMIDUN_P", "ref:lukr:ASTAND", "ref:lukr:AR_LAGT",
			"ref:lukr:AR_LAGF", "ref:lukr:BREIDD", "ref:lukr:TEG", "ref:lukr:SVF",
			"ref:lukr:RUTT_BREID", "ref:lukr:UPPR", "ref:lukr:STIGAFLOKK", "ref:lukr:DAGS_BREYT",
			"ref:lukr:NAKV_XY", "ref:lukr:AR_ADG", "capacity:pupils", "ref:lukr:DAGS_UPPF",
			"ref:lukr:OBJECTID", "ref:lukr:FLOKKUR", "ref:lukr:DAGS_UPPR", "sourcedb:id",
			"ref:lukr:NOTANDI", "mofa", "capacity:teachers", "fire_operator", "massgis:COMMENTS",
			"ref:lukr:GAGNA_EIGN", "category", "source:loc", "fireplace", "gns:UFI", "gns:ADM1",
			"gns:UNI", "gns:DSG", "alt_name:en", "Shape_Leng", "ts_orientacion", "ts_calle",
			"ts_codigo", "ts_hacia", "j3", "istat_id", "ts_desde", "building:facade:colour",
			"color", "source_type_walking_paper", "clc:code_06", "building:roof:colour",
			"NHD:WBAreaComI", "restaurant", "floating", "fire_hydrant:pressure", "time",
			"seamark:beacon_lateral:category", "toilets:number", "openGeoDB:telephone_area_code",
			"ref:zh", "name:cs", "design:code:SPb", "crossing:barrier", "ramp", "sloped_curb",
			"source:bridge", "it:fvg:regional_catalog:corsi_acqua:bacino", "prow_ref", "fdot:fihs",
			"unmil:id", "source:user", "source:location", "buoy", "data_zmiany", "is_in:clan",
			"is_in:commune", "school:ML:academie", "is_in:cercle", "school:ML:cap", "capital",
			"building:level", "taxon:species:varietas", "code", "seamark:light:1:colour", "stile",
			"recycling:glass_bottles"));

	/**
	 * This is only the starter class.
	 */
	private Main() {
		super();
	}

	/**
	 * Starter for the Generator
	 * 
	 * @param args
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
	public static void main(final String[] args) throws IOException {
		File outputFile = null;
		File inputFile = null;
		File tempFile = null;
		final Logger helpLogger = LogManager.getLogger("helplogger");

		boolean expectInputFile = false;
		boolean expectOutputFile = false;
		boolean expectTempFolder = false;
		boolean expectXMLFile = false;
		boolean printUsage = false;
		StreamIo.setDictionary(tags);
		for (final String arg : args) {
			if ("-i".equals(arg)) {
				expectInputFile = true;
			} else if ("-o".equals(arg)) {
				expectOutputFile = true;
			} else if ("-t".equals(arg)) {
				expectTempFolder = true;
			} else if (expectInputFile) {
				expectInputFile = false;
				inputFile = new File(arg);
			} else if (expectOutputFile) {
				expectOutputFile = false;
				outputFile = new File(arg);
			} else if (expectTempFolder) {
				expectTempFolder = false;
				tempFile = new File(arg);
			} else if ("--in-osm".equals(arg)) {
				expectXMLFile = true;
			} else if ("--in-o5m".equals(arg)) {
				expectXMLFile = false;
			} else if ("--help".equals(arg)) {
				printUsage = true;
			}
		}
		if (tempFile == null) {
			tempFile = new File(System.getProperty("java.io.tmpdir"));
		}
		if (!(tempFile.exists() || tempFile.isDirectory())) {
			helpLogger.debug("Temp Folder does not exist");
		}
		if (outputFile == null) {
			outputFile = new File("./");
		}

		if (inputFile == null) {
			helpLogger.debug("Missing Input File");
			printUsage = true;
		}

		if (printUsage) {
			helpLogger
					.debug("usage : java -jar oc.resolve.jar -i [Input File] -o [Output Folder] -t [Temp Folder], --in-osm --in-o5m");
		} else {
			final Generator generator = new Generator(outputFile, tempFile);
			generator.readFile(inputFile, expectXMLFile);
		}
	}
}
