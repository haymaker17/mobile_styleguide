//
//  EntityWarningMessages.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/30/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityWarningMessages : NSManagedObject

@property (nonatomic, retain) NSString * behaviorText;
@property (nonatomic, retain) NSString * behaviorTitle;
@property (nonatomic, retain) NSString * privacyText;
@property (nonatomic, retain) NSString * privacyTextShort;
@property (nonatomic, retain) NSString * privacyTitle;
@property (nonatomic, retain) NSString * warningText;
@property (nonatomic, retain) NSString * warningTextShort;
@property (nonatomic, retain) NSString * warningTitle;

@end
