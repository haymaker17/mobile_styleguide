//
//  GovDutyLocationVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/16/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HotelLocationViewController.h"
#import "FieldEditDelegate.h"
#import "GovLocation.h"

@interface GovDutyLocationVC : HotelLocationViewController <FieldEditDelegate>
{
    NSArray                 *locations;
    NSString                *actionAfterCompletion;
    NSDecimalNumber         *perDiemLdgRate;
    GovLocation             *selectedLocation;
    
    NSArray                 *taFields;  // Two fields, auth and perDiem
	id<FieldEditDelegate>	__weak _delegate;
    
    BOOL                    needPerDiemRate;
}
@property (nonatomic, strong) NSArray                   *locations;
@property (nonatomic, strong) NSDecimalNumber           *perDiemLdgRate;
@property (nonatomic, strong) GovLocation               *selectedLocation;

@property (nonatomic, strong) NSString                  *actionAfterCompletion;
@property (nonatomic, strong) NSArray                   *taFields;
@property (weak, nonatomic) id<FieldEditDelegate>       delegate;
@property BOOL needPerDiemRate;

+ (void) showDutyLocationVC:(UIViewController*)pvc withCompletion:(NSString*)action withFields:(NSArray*) taFlds withDelegate:(id<FieldEditDelegate>) del withPerDiemRate:(BOOL)needRate asRoot:(BOOL)isRoot;

@end
