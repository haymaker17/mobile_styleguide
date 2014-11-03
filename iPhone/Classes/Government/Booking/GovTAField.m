//
//  GovTAField.m
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "GovTAField.h"

#define GOV_AUTH_FIELD_ID       @"GovAuth"
#define GOV_PER_DIEM_FIELD_ID   @"GovPerDiem"

#define GOV_PER_DIEM_LDG_RATE_KEY @"GOV_PER_DIEM_LDG_RATE"
#define GOV_PER_DIEM_LOC_STATE_KEY @"GOV_PER_DIEM_LOC_STATE"  // e.g. "WA"
#define GOV_PER_DIEM_LOCATION_KEY @"GOV_PER_DIEM_LOCATION"    // e.g. "SEATTLE"
#define GOV_PER_DIEM_LOC_ZIP_KEY @"GOV_PER_DIEM_LOC_ZIP"

#define GOV_PER_DIEM_EXP_DATE_KEY @"GOV_PER_DIEM_EXP_DATE"
#define GOV_TA_DOC_TYPE_KEY @"GOV_TA_DOC_TYPE"
#define GOV_TA_DOC_NAME_KEY @"GOV_TA_DOC_NAME"

// Place to store existing trip defaults (tripKey, dates, location) for add car/hotel
// Usually attached to PER_DIEM field
#define GOV_TRIP_DEFAULTS @"GOV_TRIP_DEFAULTS"

#define GOV_TA_SPECIAL_TA_NUM @"GOV_TA_SPECIAL_TA_NUM"
// Values for special TA Num field
#define GOV_TA_NEW_AUTH @"GOV_TA_NEW_AUTH"
#define GOV_TA_USE_EXISTING @"GOV_TA_USE_EXISTING"
#define GOV_TA_GROUP_AUTH @"GOV_TA_GROUP_AUTH"

@implementation GovTAField
@dynamic perDiemLdgRate, perDiemLocationId, perDiemLocationName, perDiemLocState, perDiemLocZip, perDiemLocation, perDiemExpDate;
@dynamic tANum, tANumName, tADocType, tADocName, isNewTANum, useExisting;//, useGroupAuth;
@dynamic tripDefaults;
@synthesize useGroupAuth;
@synthesize isUSContiguous;

-(BOOL) isAuthField
{
    return [self.iD isEqualToString:GOV_AUTH_FIELD_ID];
}

-(BOOL) isPerDiemLocationField
{
    return [self.iD isEqualToString:GOV_PER_DIEM_FIELD_ID];
}

-(NSString*) perDiemLocationId
{
    return self.liKey;
}

-(void) setPerDiemLocationId:(NSString *)perDiemLocationId
{
    self.liKey = perDiemLocationId;
}

-(NSDecimalNumber*) perDiemLdgRate
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSDecimalNumber*) [dict objectForKey:GOV_PER_DIEM_LDG_RATE_KEY];
}

-(void) setPerDiemLdgRate:(NSDecimalNumber *)rate
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;

    if (rate != nil)
        [dict setObject:rate forKey:GOV_PER_DIEM_LDG_RATE_KEY];
    else
        [dict removeObjectForKey:GOV_PER_DIEM_LDG_RATE_KEY];
}

-(NSString*) perDiemLocState
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSString*) [dict objectForKey:GOV_PER_DIEM_LOC_STATE_KEY];
}

-(void) setPerDiemLocState:(NSString *)locState
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    
    if (locState != nil)
        [dict setObject:locState forKey:GOV_PER_DIEM_LOC_STATE_KEY];
    else
        [dict removeObjectForKey:GOV_PER_DIEM_LOC_STATE_KEY];
}

-(NSString*) perDiemLocZip
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSString*) [dict objectForKey:GOV_PER_DIEM_LOC_ZIP_KEY];
}

-(void) setPerDiemLocZip:(NSString *)locZip
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    
    if (locZip != nil)
        [dict setObject:locZip forKey:GOV_PER_DIEM_LOC_ZIP_KEY];
    else
        [dict removeObjectForKey:GOV_PER_DIEM_LOC_ZIP_KEY];
}

-(NSString*) perDiemLocation
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSString*) [dict objectForKey:GOV_PER_DIEM_LOCATION_KEY];
}

-(void) setPerDiemLocation:(NSString *)location
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    
    if (location != nil)
        [dict setObject:location forKey:GOV_PER_DIEM_LOCATION_KEY];
    else
        [dict removeObjectForKey:GOV_PER_DIEM_LOCATION_KEY];
}

-(NSDate*) perDiemExpDate
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSDate*) [dict objectForKey:GOV_PER_DIEM_EXP_DATE_KEY];
}

-(void) setPerDiemExpDate:(NSDate *)expDate
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    
    if (expDate != nil)
        [dict setObject:expDate forKey:GOV_PER_DIEM_EXP_DATE_KEY];
    else
        [dict removeObjectForKey:GOV_PER_DIEM_EXP_DATE_KEY];
}

-(NSString*) perDiemLocationName // Display
{
    return self.fieldValue;
}

-(void) setPerDiemLocationName:(NSString *)perDiemLocationName
{
    self.fieldValue = perDiemLocationName;
}

-(NSMutableDictionary*) tripDefaults
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return (NSMutableDictionary*) [dict objectForKey:GOV_TRIP_DEFAULTS];
}

-(void) setTripDefaults:(NSMutableDictionary *)tripDefaults
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    if (tripDefaults != nil)
        [dict setObject:tripDefaults forKey:GOV_TRIP_DEFAULTS];
    else
        [dict removeObjectForKey:GOV_TRIP_DEFAULTS];
}
-(BOOL) isNewTANum
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
    return [GOV_TA_NEW_AUTH isEqualToString:specialTaNum];
}

-(void) setIsNewTANum:(BOOL) flag
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    if (flag)
        [dict setObject:GOV_TA_NEW_AUTH forKey:GOV_TA_SPECIAL_TA_NUM];
    else
    {
        NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
        if ([GOV_TA_NEW_AUTH isEqualToString:specialTaNum])
        {
            [dict removeObjectForKey:GOV_TA_SPECIAL_TA_NUM];
        }
    }
}

-(BOOL) useExisting
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
    return [GOV_TA_USE_EXISTING isEqualToString:specialTaNum];
}

-(void) setUseExisting:(BOOL) flag
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    if (flag)
        [dict setObject:GOV_TA_USE_EXISTING forKey:GOV_TA_SPECIAL_TA_NUM];
    else
    {
        NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
        if ([GOV_TA_USE_EXISTING isEqualToString:specialTaNum])
        {
            [dict removeObjectForKey:GOV_TA_SPECIAL_TA_NUM];
        }
    }
}

//MOB-12404 Display message after Group Auth Booking
//- (BOOL) useGroupAuth
//{
//    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
//    NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
//    return [GOV_TA_GROUP_AUTH isEqualToString:specialTaNum];
//}
//
//- (void) setGroupAuth:(BOOL) flag
//{
//    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
//    if (flag)
//        [dict setObject:GOV_TA_GROUP_AUTH forKey:GOV_TA_SPECIAL_TA_NUM];
//    else
//    {
//        NSString* specialTaNum = (NSString*) [dict objectForKey:GOV_TA_SPECIAL_TA_NUM];
//        if ([GOV_TA_GROUP_AUTH isEqualToString:specialTaNum])
//        {
//            [dict removeObjectForKey:GOV_TA_SPECIAL_TA_NUM];
//        }
//    }
//}

-(NSString*) tANum
{
    return self.liKey;
}

-(void) setTANum:(NSString *)tANum
{
    self.liKey = tANum;
}

-(NSString*) tANumName // Display
{
    return self.fieldValue;
}

-(void) setTANumName:(NSString *)tANumName
{
    self.fieldValue = tANumName;
}

-(NSString*) tADocName
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return(NSString*) [dict objectForKey:GOV_TA_DOC_NAME_KEY];
}

-(void) setTADocName:(NSString *)tADocName
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    if (tADocName!= nil)
        [dict setObject:tADocName forKey:GOV_TA_DOC_NAME_KEY];
    else
        [dict removeObjectForKey:GOV_TA_DOC_NAME_KEY];
}

-(NSString*) tADocType
{
    NSDictionary* dict = (NSDictionary*) self.extraDisplayInfo;
    return(NSString*) [dict objectForKey:GOV_TA_DOC_TYPE_KEY];
}

-(void) setTADocType:(NSString*) docType
{
    NSMutableDictionary* dict = (NSMutableDictionary*) self.extraDisplayInfo;
    if (docType != nil)
        [dict setObject:docType forKey:GOV_TA_DOC_TYPE_KEY];
    else
        [dict removeObjectForKey:GOV_TA_DOC_TYPE_KEY];
}

+(GovTAField*) makeAuthField:(BOOL)isNew withTANum:(NSString*)num withName:(NSString*)name withDocName:(NSString*) docName withDocType:(NSString*) docType
{
    GovTAField * result = [[GovTAField alloc] init];
    result.iD = GOV_AUTH_FIELD_ID;
    result.dataType = GOV_AUTH_FIELD_ID;
    result.ctrlType = @"edit";
    result.liKey = num;
    result.fieldValue = name;
    result.label = [@"Selected Authorization for Travel" localize];
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys: nil];
    if (docName != nil)
        [dict setObject:docName forKey:GOV_TA_DOC_NAME_KEY];
    if (docType != nil)
        [dict setObject:docType forKey:GOV_TA_DOC_TYPE_KEY];
    result.extraDisplayInfo = dict;
    result.isNewTANum = YES;
    return result;
}

+(GovTAField*) makePerDiemField:(NSString*) locId withName:(NSString*)locName withLdgRate:(NSDecimalNumber*)rate
{
    GovTAField * result = [[GovTAField alloc] init];
    result.iD = GOV_PER_DIEM_FIELD_ID;
    result.dataType = GOV_PER_DIEM_FIELD_ID;
    result.ctrlType = @"edit";
    result.liKey = locId;
    result.fieldValue = locName;
    result.label = [@"TDY Per-Diem Location" localize];
    NSMutableDictionary *dict = [[NSMutableDictionary alloc] initWithObjectsAndKeys:rate, GOV_PER_DIEM_LDG_RATE_KEY, nil];
    result.extraDisplayInfo = dict;
    return result;
}

+(NSArray*) makeEmptyTAFields
{
    GovTAField *taFld = [self makeAuthField:NO withTANum:nil withName:nil withDocName:nil withDocType:nil];
    GovTAField *pdFld = [self makePerDiemField:nil withName:nil withLdgRate:nil];
    return [NSArray arrayWithObjects:taFld, pdFld, nil];
}

+(GovTAField*) getAuthField:(NSArray*) taFields
{
    for (GovTAField *fld in taFields)
    {
        if ([fld isAuthField])
            return fld;
    }
    return nil;
}

+(GovTAField*) getPerDiemField:(NSArray*)taFields
{
    for (GovTAField *fld in taFields)
    {
        if ([fld isPerDiemLocationField])
            return fld;
    }
    return nil;
}

// Data used for booking
+(NSString*) getExistingTANumber:(NSArray*) taFields
{
    GovTAField *taFld = [self getAuthField:taFields];
    return taFld.tANum;
}

+(NSString*) getPerdiemLocationID:(NSArray*) taFields
{
    GovTAField *taFld = [self getPerDiemField:taFields];
    return taFld.perDiemLocationId;
}

@end
