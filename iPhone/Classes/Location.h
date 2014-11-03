//
//  Location.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 2/1/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "XMLBase.h"



@class AddressComponent;
@class Coordinate;
@class BoundingBox;
@interface Location : XMLBase <NSXMLParserDelegate>
{
	NSXMLParser			*dataParser;
	NSString			*currentElement;
	NSString			*status;
	BOOL				parsedResultNode;
	NSMutableArray		*addressComponents;
	AddressComponent	*currentAddressComponent;
	NSString			*currentType;
	NSMutableDictionary	*typeDictionary; // Key = type name, Value = AddressComponent
	BOOL				parsingLocationNode;
	BOOL				parsingViewportNode;
	BOOL				parsingSouthwestNode;
	BOOL				parsingNortheastNode;
	
	NSString			*streetAddress;
	NSString			*city;
	NSString			*state;
	NSString			*country;
	NSString			*zip;
	
    NSString            *stateAbbrev;
    NSString            *countryAbbrev;
    
	Coordinate			*location;
	BoundingBox			*viewport;
    
    NSMutableString		*buildString; // MOB-8466

}

@property (nonatomic, strong) NSXMLParser			*dataParser;
@property (nonatomic, strong) NSString				*currentElement;
@property (nonatomic, strong) NSString				*status;
@property (nonatomic, assign) BOOL					parsedResultNode;
@property (nonatomic, strong) NSMutableArray		*addressComponents;
@property (nonatomic, strong) AddressComponent		*currentAddressComponent;
@property (nonatomic, strong) NSString				*currentType;
@property (nonatomic, strong) NSMutableDictionary	*typeDictionary;
@property (nonatomic, assign) BOOL					parsingLocationNode;
@property (nonatomic, assign) BOOL					parsingViewportNode;
@property (nonatomic, assign) BOOL					parsingSouthwestNode;
@property (nonatomic, assign) BOOL					parsingNortheastNode;

@property (nonatomic, strong) NSString				*streetAddress;
@property (nonatomic, strong) NSString				*city;
@property (nonatomic, strong) NSString				*state;
@property (nonatomic, strong) NSString				*country;
@property (nonatomic, strong) NSString				*zip;
@property (nonatomic, strong) NSString				*stateAbbrev;
@property (nonatomic, strong) NSString				*countryAbbrev;

@property (nonatomic, strong) Coordinate			*location;
@property (nonatomic, strong) BoundingBox			*viewport;

@property (nonatomic, strong) NSMutableString       *buildString;

-(void) parseXMLFileAtURL:(NSString *)URL; 
-(void) parseXMLFileAtData:(NSData *)webData;

-(AddressComponent*) getAddressComponentOfType:(NSString*)type;

@end



@interface AddressComponent : NSObject
{
	NSString			*formattedAddress;
	NSString			*longName;
	NSString			*shortName;
	NSMutableArray		*types;
}

@property (nonatomic, strong) NSString			*formattedAddress;
@property (nonatomic, strong) NSString			*longName;
@property (nonatomic, strong) NSString			*shortName;
@property (nonatomic, strong) NSMutableArray	*types;

@end



@interface Coordinate : NSObject
{
	NSString	*latitude;
	NSString	*longitude;
}

@property (nonatomic, strong) NSString	*latitude;
@property (nonatomic, strong) NSString	*longitude;

@end



@interface BoundingBox : NSObject
{
	Coordinate	*southwest;
	Coordinate	*northeast;
}

@property (nonatomic, strong) Coordinate	*southwest;
@property (nonatomic, strong) Coordinate	*northeast;

@end



