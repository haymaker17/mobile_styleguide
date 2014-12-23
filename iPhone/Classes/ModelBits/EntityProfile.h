//
//  EntityProfile.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityProfileBankAccount, EntityProfileCreditCard, EntityProfileEmergencyContacts;

@interface EntityProfile : NSManagedObject

@property (nonatomic, retain) NSString * firstName;
@property (nonatomic, retain) NSString * lastName;
@property (nonatomic, retain) NSString * workAddress;
@property (nonatomic, retain) NSString * city;
@property (nonatomic, retain) NSString * state;
@property (nonatomic, retain) NSString * zipCode;
@property (nonatomic, retain) NSString * country;
@property (nonatomic, retain) NSString * phoneNo;
@property (nonatomic, retain) NSSet *relEmergencyContact;
@property (nonatomic, retain) NSSet *relBancAccount;
@property (nonatomic, retain) NSSet *relCreditCard;
@end

@interface EntityProfile (CoreDataGeneratedAccessors)

- (void)addRelEmergencyContactObject:(EntityProfileEmergencyContacts *)value;
- (void)removeRelEmergencyContactObject:(EntityProfileEmergencyContacts *)value;
- (void)addRelEmergencyContact:(NSSet *)values;
- (void)removeRelEmergencyContact:(NSSet *)values;

- (void)addRelBancAccountObject:(EntityProfileBankAccount *)value;
- (void)removeRelBancAccountObject:(EntityProfileBankAccount *)value;
- (void)addRelBancAccount:(NSSet *)values;
- (void)removeRelBancAccount:(NSSet *)values;

- (void)addRelCreditCardObject:(EntityProfileCreditCard *)value;
- (void)removeRelCreditCardObject:(EntityProfileCreditCard *)value;
- (void)addRelCreditCard:(NSSet *)values;
- (void)removeRelCreditCard:(NSSet *)values;

@end
