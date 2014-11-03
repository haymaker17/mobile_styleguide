//
//  EntityPolicyExpenseType.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 11/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityPolicyExpenseType : NSManagedObject

@property (nonatomic, retain) NSString * polKey;
@property (nonatomic, retain) NSString * expKey;
@property (nonatomic, retain) NSString * expCode;
@property (nonatomic, retain) NSString * expName;
@property (nonatomic, retain) NSString * formKey;
@property (nonatomic, retain) NSString * itemizationUnallowExpKeys;
@property (nonatomic, retain) NSString * itemizeFormKey;
@property (nonatomic, retain) NSString * itemizeStyle;
@property (nonatomic, retain) NSString * itemizeType;
@property (nonatomic, retain) NSString * parentExpKey;
@property (nonatomic, retain) NSString * parentExpName;
@property (nonatomic, retain) NSString * supportsAttendees;
@property (nonatomic, retain) NSString * userAsAtnDefault;
@property (nonatomic, retain) NSString * allowEditAtnAmt;
@property (nonatomic, retain) NSString * allowEditAtnCount;
@property (nonatomic, retain) NSString * allowNoShows;
@property (nonatomic, retain) NSString * displayAddAtnOnForm;
@property (nonatomic, retain) NSString * displayAtnAmounts;
@property (nonatomic, retain) NSString * unallowAtnTypeKeys;
@property (nonatomic, retain) NSString * access;
@property (nonatomic, retain) NSString * version;

@end
