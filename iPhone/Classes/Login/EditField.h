//
//  EditField.h
//  ConcurMobile
//
//  Created by Paul Kramer on 11/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface EditField : NSObject
{
    NSString *name, *value;
    UIImage *img;
}

@property (strong, nonatomic) NSString *name;
@property (strong, nonatomic) NSString *value;
@property (strong, nonatomic) UIImage *img;

@end
