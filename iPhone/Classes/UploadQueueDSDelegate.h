//
//  UploadQueueDSDelegate.h
//  ConcurMobile
//
//  Created by charlottef on 11/9/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityMobileEntry.h"

@protocol UploadQueueDSDelegate <NSObject>

-(void) didSelectMobileEntry:(EntityMobileEntry*)mobileEntry;
-(void) didSelectReceiptWithId:(NSString*)receiptId;
-(void) showNoDataView;
-(void) hideNoDataView;

@end
