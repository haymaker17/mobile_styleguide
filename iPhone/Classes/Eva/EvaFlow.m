//
//  EvaFlow.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/15/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//
/*
 This class is not used yet, Hotel Search or other search should look at Flow to identify the list of related locations 
 Destination location should be decided based on location index given in the related locations. 
 
 */
#import "EvaFlow.h"

@interface EvaFlow ()

@property (nonatomic,strong) NSString *questionType;
@property (nonatomic,strong) NSString *questionCategory;
@property (nonatomic,strong) NSString *questionSubCategory;


@property (nonatomic,strong) NSDictionary *evaflowDict;

@end

@implementation EvaFlow

-(id)initWithDict:(NSDictionary *)dictionary
{
    self = [super init];
    if(self)
    {
        self.evaflowDict = [[NSDictionary alloc] initWithDictionary:dictionary];
        [self parseJson];
    }
    return self;
    
}

-(void)parseJson
{
    
    NSArray *relatedlocations = self.evaflowDict[@"RelatedLocations"];
    // Related locations is an integer array.
    if([relatedlocations count]>0)
    {
        self.evaRelatedLocations =  [[NSArray alloc]initWithArray:relatedlocations];
    }
    if(self.evaflowDict[@"QuestionCategory"])
    {
        self.questionCategory = self.evaflowDict[@"QuestionCategory"];
    }
    if(self.evaflowDict[@"QuestionSubCategory"])
    {
        self.questionSubCategory = self.evaflowDict[@"QuestionSubCategory"];
    }

    if(self.evaflowDict[@"QuestionType"])
    {
        self.questionType = self.evaflowDict[@"QuestionType"];
    }

    if(self.evaflowDict[@"Type"])
    {
        NSString *type = self.evaflowDict[@"Type"];
        if([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_HOTELS])
        {
            self.evaFlowType = EVA_HOTELS;
        }
        else if([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_FLIGHTS])
        {
            self.evaFlowType = EVA_FLIGHTS;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_CARS])
        {
            self.evaFlowType = EVA_CARS;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_TRAINS])
        {
            self.evaFlowType = EVA_TRAINS;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_QUESTION])
        {
            self.evaFlowType = EVA_QUESTION;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_STATEMENT])
        {
            self.evaFlowType = EVA_STATEMENT;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_GREETING])
        {
            self.evaFlowType = EVA_GREETING;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_ANSWER])
        {
            self.evaFlowType = EVA_ANSWER;
        }
    }
    if(self.evaflowDict[@"ActionType"])
    {
        NSString *type = self.evaflowDict[@"ActionType"];
        if([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_HOTELS])
        {
            self.actionType = EVA_HOTELS;
        }
        else if([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_FLIGHTS])
        {
            self.actionType = EVA_FLIGHTS;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_CARS])
        {
            self.actionType = EVA_CARS;
        }
        else if ([type lengthIgnoreWhitespace] && [type isEqualToString:EVA_FLOW_TRAINS])
        {
            self.actionType = EVA_TRAINS;
        }
     }

    if(self.evaflowDict[@"SayIt"])
    {
        self.sayIt = self.evaflowDict[@"SayIt"];
    }


//    for (NSDictionary *relLocation in relatedlocations) {
//        EvaRelatedLocation *evaRelLocation = [[EvaRelatedLocation alloc]initWithDict:relLocation];
//        [self.evaRelatedLocations addObject:evaRelLocation];
//    }
    

}

@end
