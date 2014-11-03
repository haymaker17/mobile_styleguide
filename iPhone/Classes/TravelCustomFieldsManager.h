//
//  TravelCustomFieldsManager.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/15/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityTravelCustomFields.h"
#import "EntityTravelCustomFieldAttribute.h"

@interface TravelCustomFieldsManager : NSObject {
    NSManagedObjectContext      *_context;
    NSString *entityName;
}

@property (nonatomic, strong) NSManagedObjectContext *context;
@property (nonatomic, strong) NSString *entityName;

-(void) saveIt:(NSManagedObject *) obj;
-(BOOL) hasAny;
-(NSManagedObject *) makeNew;
-(NSArray *) fetchAll;
-(NSManagedObject *) fetchOrMake:(NSString *)key;
-(void) deleteObj:(NSManagedObject *)obj;
-(void) deleteAll;

+(TravelCustomFieldsManager*)sharedInstance;
-(id)init;

-(NSManagedObject *) fetchById:(NSString *)key;
-(NSManagedObject *) fetchByAttributeId:(NSString *)key;
-(NSManagedObject *) fetchOrMakeAttribute:(NSString *)key;
-(EntityTravelCustomFieldAttribute *) makeNewAttribute;

-(int) getNumberOfFields;
-(int) getNumberOfAttributesForFieldId:(NSString *)attributeId;
-(NSInteger) getCustomFieldIndex:(EntityTravelCustomFields *)field forAttributeValue:(NSString *)value;
-(BOOL) hasPendingRequiredTripFields;
-(BOOL) hasPendingRequiredTripFieldsAtStart:(BOOL)atStart;

-(NSArray *) fetchAllRequiredFields;
-(NSArray *) fetchAllRequiredFieldsAtStart:(BOOL)atStart;

-(NSArray *) fetchAllFieldsWithAttributeValue;

-(NSArray *) fetchAllFieldsAtStart:(BOOL)atStart;

+(NSString *)makeCustomFieldsRequestXMLBody;
@end
