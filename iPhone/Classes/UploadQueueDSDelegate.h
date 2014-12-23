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
    // MOB-21462: modify this delegat for showing pdf receipt
-(void) didSelectReceiptWithId:(NSString*)receiptId isPdfReceipt:(BOOL)isPdfReceipt;
-(void) showNoDataView;
-(void) hideNoDataView;

@end
