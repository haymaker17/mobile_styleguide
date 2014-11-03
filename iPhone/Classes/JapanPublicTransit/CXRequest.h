//
//  CXRequest.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface CXRequest : NSObject

@property (nonatomic, copy) NSString *method;
@property (nonatomic, copy) NSString *path;
@property (nonatomic, copy) NSString *requestXML;

- (id)initWithServicePath:(NSString *)path requestXML:(NSString *)requestXML;
- (id)initWithServicePath:(NSString *)path;

@end
