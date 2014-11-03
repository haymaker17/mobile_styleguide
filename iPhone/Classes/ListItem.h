//
//  ListItem.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface ListItem : NSObject 
{
	NSString                *liName, *liCode, *liKey;
	NSString                *isMru;	// Y, L - last MRU element
	NSMutableDictionary		*fields;	// Extra info associated with this list item, e.g. reportFormKey for policy
    NSString                *external;
}

@property (strong, nonatomic) NSString              *liCode;
@property (strong, nonatomic) NSString              *liKey;
@property (strong, nonatomic) NSString              *liName;
@property (strong, nonatomic) NSString              *isMru;
@property (strong, nonatomic) NSString              *external;
@property (strong, nonatomic) NSMutableDictionary   *fields;

+(NSArray*) sortByName:(NSArray*)listItems;
+(NSArray*) sortByCode:(NSArray*)listItems;
+(NSArray*) sortByIntKey:(NSArray*)listItems;
+(NSArray*) sortByKey:(NSArray*)listItems;

- (id)initWithCoder:(NSCoder *)coder;
- (void)encodeWithCoder:(NSCoder *)coder;

@end
