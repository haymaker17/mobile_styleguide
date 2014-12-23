//
//  EntityProfileEmergencyContacts.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityProfile;

@interface EntityProfileEmergencyContacts : NSManagedObject

@property (nonatomic, retain) NSString * fistName;
@property (nonatomic, retain) NSString * lastName;
@property (nonatomic, retain) NSString * phoneNo;
@property (nonatomic, retain) NSString * relation;
@property (nonatomic, retain) EntityProfile *relProfile;

@end
