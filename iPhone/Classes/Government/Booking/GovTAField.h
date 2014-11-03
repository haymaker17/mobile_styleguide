//
//  GovTAField.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/18/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FormFieldData.h"
@interface GovTAField : FormFieldData
{
    BOOL useGroupAuth;
}

@property (nonatomic, strong) NSString              *perDiemLocationId;
@property (nonatomic, strong) NSString              *perDiemLocState;
@property (nonatomic, strong) NSDate                *perDiemExpDate;
@property (nonatomic, strong) NSString              *perDiemLocationName;
@property (nonatomic, strong) NSString              *perDiemLocation;
@property (nonatomic, strong) NSString              *perDiemLocZip;
@property (nonatomic, strong) NSDecimalNumber       *perDiemLdgRate;

@property (nonatomic, strong) NSString              *tANum;
@property (nonatomic, strong) NSString              *tANumName;
@property (nonatomic, strong) NSString              *tADocType;
@property (nonatomic, strong) NSString              *tADocName;
@property (nonatomic, strong) NSMutableDictionary   *tripDefaults;
@property BOOL isUSContiguous;

@property BOOL isNewTANum;
@property BOOL useExisting;
@property BOOL useGroupAuth;

-(BOOL) isAuthField;
-(BOOL) isPerDiemLocationField;


+(GovTAField*) makeAuthField:(BOOL)isNew withTANum:(NSString*)num withName:(NSString*)name withDocName:(NSString*) docName withDocType:(NSString*) docType;

+(GovTAField*) makePerDiemField:(NSString*) locId withName:(NSString*)locName withLdgRate:(NSDecimalNumber*)rate;

+(NSArray*) makeEmptyTAFields;

// Data used for booking
+(GovTAField*) getAuthField:(NSArray*) taFields;
+(GovTAField*) getPerDiemField:(NSArray*)taFields;
+(NSString*) getExistingTANumber:(NSArray*) taFields;
+(NSString*) getPerdiemLocationID:(NSArray*) taFields;

@end
