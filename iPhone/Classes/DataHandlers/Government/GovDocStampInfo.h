//
//  GovDocStampInfo.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/20/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovDocStampInfo : NSObject
{
    NSString            *stampName; //stamp
    NSNumber            *isDefault; // default_stamp
    NSNumber            *returnToRequired; //returnto_required
}

@property (nonatomic, strong) NSString              *stampName;
@property (nonatomic, strong) NSNumber              *isDefault;
@property (nonatomic, strong) NSNumber              *returnToRequired;


@end
