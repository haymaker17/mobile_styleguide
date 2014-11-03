//
//  MultiWebLinkData.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 11/2/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface MultiWebLinkData : NSObject {
    NSString *title;
    NSString *actionURL;
}

@property (nonatomic, strong) NSString *title;
@property (nonatomic, strong) NSString *actionURL;
@end
