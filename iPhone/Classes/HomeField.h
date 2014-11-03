//
//  HomeField.h
//  ConcurMobile
//
//  Created by Paul Kramer on 4/11/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HomeField : NSObject {
    NSString            *heading, *subHeading, *selectorName, *imageName;
}

@property (strong, nonatomic)   NSString    *heading;
@property (strong, nonatomic)   NSString    *subHeading;
@property (strong, nonatomic)   NSString    *selectorName;
@property (strong, nonatomic)   NSString    *imageName;
@end
