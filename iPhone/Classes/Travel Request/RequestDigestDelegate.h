//
//  RequestDigestDelegate.h
//  ConcurMobile
//
//  Created by laurent mery on 17/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//
/*
 
 This delegate is used to send datas after any action
 to refresh previous view (caller)
 
 */

#import <Foundation/Foundation.h>
@class CTETravelRequest;

@protocol RequestDigestDelegate <NSObject>

@optional

-(void)digestDidDismissOnAction:(NSString *)action andRequestData:(CTETravelRequest *)request;

@end



