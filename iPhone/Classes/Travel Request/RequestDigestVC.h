//
//  RequestDigestVC.h
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCViewController.h"
#import "RequestDigestDelegate.h"

@class CTETravelRequest;

@interface RequestDigestVC : CCViewController

@property(nonatomic, assign) id<RequestDigestDelegate> delegate;

@property (retain, nonatomic) CTETravelRequest *request;

@end
