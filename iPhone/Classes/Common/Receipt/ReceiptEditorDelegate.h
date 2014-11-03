//
//  ReceiptEditorDelegate.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/26/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Receipt.h"

@protocol ReceiptEditorDelegate <NSObject>

-(void) receiptUpdated:(Receipt*) receipt useV2Endpoint:(BOOL)useV2Endpoint;
-(void) receiptDeleted:(Receipt*) receipt;
-(void) receiptQueued:(Receipt*) receipt;  // For offline?
-(void) receiptDisplayed:(Receipt*) receipt; // For audit events
@end
