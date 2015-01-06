//
//  FFSection.h
//  ConcurMobile
//
//  Created by laurent mery on 30/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTEFormFields.h"

@interface FFSection : NSObject

/*
 * name it's the ref to dialogue between myform and tableview where section is 0, 1...
 * myform ask to add a section with section name @"requestHeader"
 * we create section on table view (ref = 0 or 1 or 2...)
 * and myform could update data on this section just with requestheader reference without know table view ref
 */
@property (nonatomic, copy) NSString *name;


@property (nonatomic, strong) CTEFormFields *form;

@property (nonatomic, copy) NSArray *fields;
@property (nonatomic, copy) NSArray *fieldsHidden;

@property (nonatomic, strong) UIView *headerView;

@property (nonatomic, strong) UIView *footerView;

@property (nonatomic, assign) BOOL isFormEditable;


@end
