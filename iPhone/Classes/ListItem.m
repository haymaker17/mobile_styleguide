//
//  ListItem.m
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "ListItem.h"


@implementation ListItem
@synthesize liName, liKey, liCode, fields, isMru;
@synthesize external;



#pragma mark NSCoding Protocol Methods
- (void)encodeWithCoder:(NSCoder *)coder {
    [coder encodeObject:liName		forKey:@"liName"];
	[coder encodeObject:liKey		forKey:@"liKey"];
	[coder encodeObject:liCode		forKey:@"liCode"];
	[coder encodeObject:fields		forKey:@"fields"];
	[coder encodeObject:isMru		forKey:@"isMru"];
    [coder encodeObject:external    forKey:@"external"];
}


- (id)initWithCoder:(NSCoder *)coder {
	self.liName = [coder decodeObjectForKey:@"liName"];
	self.liKey = [coder decodeObjectForKey:@"liKey"];
	self.liCode = [coder decodeObjectForKey:@"liCode"];
	self.fields = [coder decodeObjectForKey:@"fields"];
	self.isMru = [coder decodeObjectForKey:@"isMru"];
    self.external = [coder decodeObjectForKey:@"external"];
    return self;
}

#pragma mark NSCopying Protocol Methods
- (id)copyWithZone:(NSZone *)zone
{
    ListItem* copy = [ListItem allocWithZone:zone];
	
	copy.liKey = self.liKey;
    copy.liCode = self.liCode;
    copy.liName = self.liName;
    copy.fields = self.fields;
    copy.isMru = self.isMru;
    copy.external = self.external;
    
    return copy;
}

// TODO's
+(NSArray*) sortByName:(NSArray*)listItems
{
	return listItems;
}

+(NSArray*) sortByCode:(NSArray*)listItems
{
	return listItems;
}

+(NSArray*) sortByIntKey:(NSArray*)listItems
{
	return listItems;
}

+(NSArray*) sortByKey:(NSArray*)listItems
{
	return listItems;
}


@end
