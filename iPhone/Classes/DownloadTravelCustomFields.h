//
//  DownloadTravelCustomFields.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "TravelCustomField.h"
#import "TravelCustomFieldAttributeValue.h"
#import "TravelCustomFieldsManager.h"
#import "EntityTravelCustomFields.h"
#import "EntityTravelCustomFieldAttribute.h"

@interface DownloadTravelCustomFields : MsgResponderCommon
{
    BOOL   hasDependency;
    BOOL   isAttributeValue;
    TravelCustomFieldAttributeValue *tcfAttribute;
    TravelCustomField     *field;
    NSMutableArray        *travelCustomFields;
    EntityTravelCustomFields *tripField;
    EntityTravelCustomFieldAttribute *tripFieldAttribute;
    
}
@property BOOL   hasDependency;
@property BOOL   isAttributeValue;
@property (nonatomic, strong) TravelCustomFieldAttributeValue *tcfAttribute;
@property (nonatomic, strong) TravelCustomField     *field;
@property (nonatomic, strong) NSMutableArray        *travelCustomFields;
@property  (nonatomic, strong) EntityTravelCustomFields *tripField;
@property  (nonatomic, strong) EntityTravelCustomFieldAttribute *tripFieldAttribute;
@property (nonatomic) BOOL isCustomFieldSearch;

-(id)init;
-(Msg*) newMsg:(NSMutableDictionary *)parameterBag;

@end
